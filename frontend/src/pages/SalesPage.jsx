import { useCallback, useEffect, useState } from 'react'
import { api, centsFromInput, formatDateTime, formatMoney } from '../services/api.js'
import { EmptyState, ErrorState, Field, LoadingState, PageHeading, StatusBadge } from '../components/PageState.jsx'

const today = () => { const d = new Date(); return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}` }

export default function SalesPage() {
  const [sales, setSales] = useState([])
  const [products, setProducts] = useState([])
  const [customers, setCustomers] = useState([])
  const [items, setItems] = useState([{ productId: '', quantity: '1' }])
  const [customerId, setCustomerId] = useState('')
  const [method, setMethod] = useState('PIX')
  const [dueDate, setDueDate] = useState(today())
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const load = useCallback(() => {
    setLoading(true); setError('')
    Promise.all([api.sales(), api.products(), api.customers()]).then(([vendas, produtos, clientes]) => { setSales(vendas); setProducts(produtos); setCustomers(clientes) })
      .catch((e) => setError(e.message)).finally(() => setLoading(false))
  }, [])
  useEffect(load, [load])
  const activeProducts = products.filter((p) => p.active)
  const total = items.reduce((sum, line) => {
    const product = activeProducts.find((p) => p.id === Number(line.productId))
    return sum + (product ? product.priceCents * Math.max(0, Number(line.quantity) || 0) : 0)
  }, 0)
  const setLine = (index, key, value) => setItems((old) => old.map((line, i) => i === index ? { ...line, [key]: value } : line))

  async function submit(e) {
    e.preventDefault(); setSaving(true); setError(''); setNotice('')
    const validItems = items.filter((line) => line.productId && Number(line.quantity) > 0)
    if (!validItems.length) { setError('Adicione pelo menos um produto com quantidade maior que zero.'); setSaving(false); return }
    try {
      const sale = await api.create('/api/vendas', { customerId: customerId ? Number(customerId) : null, items: validItems.map((line) => ({ productId: Number(line.productId), quantity: Number(line.quantity) })), paymentMethod: method, dueDate: method === 'A_PRAZO' ? dueDate : null })
      setItems([{ productId: '', quantity: '1' }]); setCustomerId(''); setMethod('PIX'); setNotice(`Venda #${sale.id} confirmada.`); load()
    } catch (e) { setError(e.message) } finally { setSaving(false) }
  }

  async function cancel(sale) {
    if (!window.confirm(`Cancelar a venda #${sale.id}? O estoque será devolvido.`)) return
    setError(''); setNotice('')
    try { await api.cancelSale(sale.id); setNotice(`Venda #${sale.id} cancelada e estoque atualizado.`); load() }
    catch (e) { setError(e.message) }
  }

  return <>
    <PageHeading eyebrow="OPERAÇÃO" title="Vendas" detail="Confirme vendas para baixar o estoque e registrar o recebimento." />
    {(error || notice) && <div className={error ? 'alert error' : 'alert success'} role={error ? 'alert' : 'status'}>{error || notice}</div>}
    <section className="content-grid">
      <article className="panel form-panel"><div className="panel-heading"><div><h3>Registrar venda</h3><p>O preço usado vem do cadastro do produto.</p></div></div>
        <form className="form-stack" onSubmit={submit}>
          <Field label="Cliente"><select value={customerId} onChange={(e) => setCustomerId(e.target.value)}><option value="">Consumidor final</option>{customers.map((c) => <option value={c.id} key={c.id}>{c.name}</option>)}</select></Field>
          <div className="line-items-heading"><strong>Produtos</strong><button className="text-button" type="button" onClick={() => setItems((old) => [...old, { productId: '', quantity: '1' }])}>+ Adicionar item</button></div>
          {items.map((line, index) => <div className="sale-line" key={index}>
            <label className="field"><span>Produto</span><select required value={line.productId} onChange={(e) => setLine(index, 'productId', e.target.value)}><option value="">Selecione</option>{activeProducts.map((p) => <option value={p.id} key={p.id} disabled={p.stock < 1}>{p.name} · {formatMoney(p.priceCents)} · estoque {p.stock}</option>)}</select></label>
            <label className="field qty-field"><span>Qtd.</span><input required type="number" min="1" step="1" value={line.quantity} onChange={(e) => setLine(index, 'quantity', e.target.value)} /></label>
            {items.length > 1 && <button className="icon-button remove-line" type="button" aria-label="Remover item" onClick={() => setItems((old) => old.filter((_, i) => i !== index))}>×</button>}
          </div>)}
          <div className="sale-total"><span>Total da venda</span><strong>{formatMoney(total)}</strong></div>
          <Field label="Forma de pagamento"><select value={method} onChange={(e) => setMethod(e.target.value)}><option value="PIX">Pix</option><option value="DINHEIRO">Dinheiro</option><option value="CARTAO_DEBITO">Cartão de débito</option><option value="CARTAO_CREDITO">Cartão de crédito</option><option value="TRANSFERENCIA">Transferência</option><option value="A_PRAZO">A prazo</option></select></Field>
          {method === 'A_PRAZO' && <><Field label="Vencimento"><input required type="date" value={dueDate} onChange={(e) => setDueDate(e.target.value)} /></Field>{!customerId && <p className="form-hint">Venda a prazo exige selecionar um cliente.</p>}</>}
          <button className="button primary" type="submit" disabled={saving || activeProducts.length === 0}>{saving ? 'Confirmando…' : 'Confirmar venda'}</button>
          {activeProducts.length === 0 && <p className="form-hint">Cadastre um produto e receba uma compra para iniciar as vendas.</p>}
        </form>
      </article>
      <article className="panel list-panel"><div className="panel-heading"><div><h3>Histórico de vendas</h3><p>Registros mais recentes primeiro</p></div></div>
        {loading ? <LoadingState /> : sales.length === 0 ? <EmptyState title="Nenhuma venda registrada" detail="As vendas confirmadas aparecerão nesta lista." /> : <div className="record-list">{sales.map((sale) => <div className="sale-record" key={sale.id}>
          <div className="record-main"><strong>Venda #{sale.id}</strong><span>{sale.customerName} · {formatDateTime(sale.createdAt)}</span><div className="inline-detail">{sale.items.map((item) => `${item.quantity} × ${item.productName}`).join(' · ')}</div></div>
          <div className="record-extra"><StatusBadge status={sale.status} /><strong>{formatMoney(sale.totalCents)}</strong></div>
          {sale.cancellable && <button className="text-button danger-text" onClick={() => cancel(sale)}>Cancelar venda</button>}
        </div>)}</div>}
      </article>
    </section>
  </>
}
