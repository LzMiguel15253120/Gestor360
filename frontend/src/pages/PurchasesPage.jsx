import { useCallback, useEffect, useState } from 'react'
import { api, centsFromInput, formatDate, formatDateTime, formatMoney } from '../services/api.js'
import { EmptyState, Field, LoadingState, PageHeading, StatusBadge } from '../components/PageState.jsx'

const today = () => { const d = new Date(); return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}` }

export default function PurchasesPage() {
  const [purchases, setPurchases] = useState([])
  const [products, setProducts] = useState([])
  const [suppliers, setSuppliers] = useState([])
  const [supplierId, setSupplierId] = useState('')
  const [dueDate, setDueDate] = useState(today())
  const [lines, setLines] = useState([{ productId: '', quantity: '1', price: '' }])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [saving, setSaving] = useState(false)
  const load = useCallback(() => {
    setLoading(true); setError('')
    Promise.all([api.purchases(), api.products(), api.suppliers()]).then(([compras, produtos, fornecedores]) => { setPurchases(compras); setProducts(produtos); setSuppliers(fornecedores) })
      .catch((e) => setError(e.message)).finally(() => setLoading(false))
  }, [])
  useEffect(load, [load])
  const total = lines.reduce((sum, line) => sum + centsFromInput(line.price || '0') * Math.max(Number(line.quantity) || 0, 0), 0)
  const setLine = (i, key, value) => setLines((old) => old.map((line, index) => index === i ? { ...line, [key]: value } : line))
  async function submit(e) {
    e.preventDefault(); setSaving(true); setError(''); setNotice('')
    try {
      const purchase = await api.create('/api/compras', { supplierId: Number(supplierId), dueDate, items: lines.map((line) => ({ productId: Number(line.productId), quantity: Number(line.quantity), unitPriceCents: centsFromInput(line.price) })) })
      setNotice(`Compra #${purchase.id} registrada. O estoque só mudará após o recebimento.`); setLines([{ productId: '', quantity: '1', price: '' }]); load()
    } catch (e) { setError(e.message) } finally { setSaving(false) }
  }
  async function receive(purchase) {
    if (!window.confirm(`Confirmar o recebimento da compra #${purchase.id}? Os produtos entrarão no estoque.`)) return
    setError(''); setNotice('')
    try { await api.receivePurchase(purchase.id); setNotice(`Compra #${purchase.id} recebida. Estoque e contas a pagar atualizados.`); load() }
    catch (e) { setError(e.message) }
  }
  return <>
    <PageHeading eyebrow="OPERAÇÃO" title="Compras" detail="Registre pedidos e confirme o recebimento para atualizar o estoque." />
    {(error || notice) && <div className={error ? 'alert error' : 'alert success'} role={error ? 'alert' : 'status'}>{error || notice}</div>}
    <section className="content-grid">
      <article className="panel form-panel"><div className="panel-heading"><div><h3>Registrar compra</h3><p>Compra pendente não altera o estoque.</p></div></div>
        <form className="form-stack" onSubmit={submit}>
          <Field label="Fornecedor"><select required value={supplierId} onChange={(e) => setSupplierId(e.target.value)}><option value="">Selecione</option>{suppliers.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}</select></Field>
          <Field label="Vencimento da conta"><input required type="date" value={dueDate} onChange={(e) => setDueDate(e.target.value)} /></Field>
          <div className="line-items-heading"><strong>Produtos</strong><button className="text-button" type="button" onClick={() => setLines((old) => [...old, { productId: '', quantity: '1', price: '' }])}>+ Adicionar item</button></div>
          {lines.map((line, index) => <div className="purchase-line" key={index}>
            <label className="field"><span>Produto</span><select required value={line.productId} onChange={(e) => setLine(index, 'productId', e.target.value)}><option value="">Selecione</option>{products.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}</select></label>
            <label className="field qty-field"><span>Qtd.</span><input required type="number" min="1" step="1" value={line.quantity} onChange={(e) => setLine(index, 'quantity', e.target.value)} /></label>
            <label className="field price-field"><span>Custo (R$)</span><input required type="number" min="0" step="0.01" value={line.price} onChange={(e) => setLine(index, 'price', e.target.value)} /></label>
            {lines.length > 1 && <button className="icon-button remove-line" type="button" aria-label="Remover item" onClick={() => setLines((old) => old.filter((_, i) => i !== index))}>×</button>}
          </div>)}
          <div className="sale-total"><span>Total estimado</span><strong>{formatMoney(total)}</strong></div>
          <button className="button primary" type="submit" disabled={saving}>{saving ? 'Salvando…' : 'Registrar compra'}</button>
        </form>
      </article>
      <article className="panel list-panel"><div className="panel-heading"><div><h3>Histórico de compras</h3><p>Pedidos registrados</p></div></div>
        {loading ? <LoadingState /> : purchases.length === 0 ? <EmptyState title="Nenhuma compra registrada" detail="Os pedidos aparecerão aqui." /> : <div className="record-list">{purchases.map((purchase) => <div className="sale-record" key={purchase.id}>
          <div className="record-main"><strong>Compra #{purchase.id} · {purchase.supplierName}</strong><span>{formatDateTime(purchase.createdAt)} · vencimento {formatDate(purchase.dueDate)}</span><div className="inline-detail">{purchase.items.map((item) => `${item.quantity} × ${item.productName}`).join(' · ')}</div></div>
          <div className="record-extra"><StatusBadge status={purchase.status} /><strong>{formatMoney(purchase.totalCents)}</strong></div>
          {purchase.status === 'PENDENTE' && <button className="button secondary small-button" onClick={() => receive(purchase)}>Receber compra</button>}
        </div>)}</div>}
      </article>
    </section>
  </>
}
