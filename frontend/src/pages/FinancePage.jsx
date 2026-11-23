import { useCallback, useEffect, useState } from 'react'
import { api, centsFromInput, formatDate, formatDateTime, formatMoney } from '../services/api.js'
import { EmptyState, ErrorState, Field, LoadingState, PageHeading, StatusBadge } from '../components/PageState.jsx'

const METHODS = [['PIX', 'Pix'], ['DINHEIRO', 'Dinheiro'], ['CARTAO_DEBITO', 'Cartão de débito'], ['CARTAO_CREDITO', 'Cartão de crédito'], ['TRANSFERENCIA', 'Transferência']]

export default function FinancePage() {
  const [type, setType] = useState('')
  const [accounts, setAccounts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [payingId, setPayingId] = useState(null)
  const [payment, setPayment] = useState({})
  const [history, setHistory] = useState({})
  const load = useCallback(() => {
    setLoading(true); setError('')
    api.accounts(type).then(setAccounts).catch((e) => setError(e.message)).finally(() => setLoading(false))
  }, [type])
  useEffect(load, [load])
  const setField = (id, key, value) => setPayment((old) => ({ ...old, [id]: { amount: '', method: 'PIX', ...old[id], [key]: value } }))
  async function pay(account) {
    const data = payment[account.id] || { amount: '', method: 'PIX' }
    const amountCents = centsFromInput(data.amount)
    if (!amountCents || amountCents < 1) { setError('Informe um valor de pagamento maior que zero.'); return }
    setError(''); setNotice(''); setPayingId(account.id)
    try { await api.registerPayment(account.id, { amountCents, paymentMethod: data.method }); setNotice(`Pagamento registrado na conta “${account.description}”.`); setPayment((old) => ({ ...old, [account.id]: { amount: '', method: 'PIX' } })); load() }
    catch (e) { setError(e.message) } finally { setPayingId(null) }
  }
  async function toggleHistory(id) {
    if (history[id]) { setHistory((old) => { const next = { ...old }; delete next[id]; return next }); return }
    setHistory((old) => ({ ...old, [id]: { loading: true } }))
    try { const data = await api.payments(id); setHistory((old) => ({ ...old, [id]: { loading: false, data } })) }
    catch (e) { setHistory((old) => ({ ...old, [id]: { loading: false, error: e.message } })) }
  }
  return <>
    <PageHeading eyebrow="FINANCEIRO" title="Contas" detail="Acompanhe vencimentos e registre pagamentos totais ou parciais." />
    {(error || notice) && <div className={error ? 'alert error' : 'alert success'} role={error ? 'alert' : 'status'}>{error || notice}</div>}
    <article className="panel"><div className="panel-heading"><div><h3>Contas a pagar e receber</h3><p>Contas são criadas por vendas a prazo e compras recebidas.</p></div><label className="filter-field"><span>Mostrar</span><select value={type} onChange={(e) => setType(e.target.value)}><option value="">Todas as contas</option><option value="RECEBER">A receber</option><option value="PAGAR">A pagar</option></select></label></div>
      {loading ? <LoadingState /> : accounts.length === 0 ? <EmptyState title="Nenhuma conta encontrada" detail="Contas de vendas a prazo e compras recebidas aparecerão aqui." /> : <div className="account-list">{accounts.map((account) => {
        const draft = payment[account.id] || { amount: '', method: 'PIX' }
        return <section className="account-card" key={account.id}>
          <div className="account-main"><div className="record-main"><strong>{account.description}</strong><span>{account.type === 'RECEBER' ? 'A receber' : 'A pagar'} · vencimento {formatDate(account.dueDate)}</span></div><div className="record-extra"><StatusBadge status={account.status} /><strong>{formatMoney(account.balanceCents)} <small>em aberto</small></strong></div></div>
          <div className="account-details"><span>Valor original: <strong>{formatMoney(account.amountCents)}</strong></span><span>Pago: <strong>{formatMoney(account.paidCents)}</strong></span><button className="text-button" onClick={() => toggleHistory(account.id)}>{history[account.id] ? 'Fechar histórico' : 'Ver pagamentos'}</button></div>
          {history[account.id] && <div className="payment-history">{history[account.id].loading ? <LoadingState /> : history[account.id].error ? <ErrorState message={history[account.id].error} /> : history[account.id].data.length ? history[account.id].data.map((p) => <div className="payment-row" key={p.id}><span>{formatDateTime(p.paidAt)} · {p.paymentMethod.replaceAll('_', ' ')}</span><strong>{formatMoney(p.amountCents)}</strong></div>) : <p className="muted-note">Nenhum pagamento registrado.</p>}</div>}
          {account.balanceCents > 0 && <div className="payment-form"><label className="field"><span>Valor a pagar/receber (R$)</span><input type="number" min="0.01" max={(account.balanceCents / 100).toFixed(2)} step="0.01" value={draft.amount} onChange={(e) => setField(account.id, 'amount', e.target.value)} /></label><label className="field"><span>Forma</span><select value={draft.method} onChange={(e) => setField(account.id, 'method', e.target.value)}>{METHODS.map(([key, label]) => <option key={key} value={key}>{label}</option>)}</select></label><button className="button secondary small-button" disabled={payingId === account.id} onClick={() => pay(account)}>{payingId === account.id ? 'Registrando…' : 'Registrar pagamento'}</button></div>}
        </section>
      })}</div>}
    </article>
  </>
}
