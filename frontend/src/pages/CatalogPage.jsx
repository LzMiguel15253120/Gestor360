import { useCallback, useEffect, useMemo, useState } from 'react'
import { api, centsFromInput, formatMoney, moneyInput } from '../services/api.js'
import { EmptyState, ErrorState, Field, LoadingState, PageHeading } from '../components/PageState.jsx'

const config = {
  products: { title: 'Produtos', singular: 'produto', route: 'produtos', fields: ['code', 'name', 'description', 'price', 'minimumStock', 'categoryId'] },
  categories: { title: 'Categorias', singular: 'categoria', route: 'categorias', fields: ['name', 'description'] },
  customers: { title: 'Clientes', singular: 'cliente', route: 'clientes', fields: ['name', 'email', 'phone'] },
  suppliers: { title: 'Fornecedores', singular: 'fornecedor', route: 'fornecedores', fields: ['name', 'email', 'phone'] },
}
const labels = { code: 'Código', name: 'Nome', description: 'Descrição', price: 'Preço de venda (R$)', minimumStock: 'Estoque mínimo', categoryId: 'Categoria', email: 'E-mail', phone: 'Telefone' }
const blank = { code: '', name: '', description: '', price: '', minimumStock: '0', categoryId: '', email: '', phone: '' }

export default function CatalogPage({ kind }) {
  const settings = config[kind]
  const [records, setRecords] = useState([])
  const [categories, setCategories] = useState([])
  const [form, setForm] = useState(blank)
  const [editing, setEditing] = useState(null)
  const [history, setHistory] = useState(null)
  const [query, setQuery] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')

  const load = useCallback(() => {
    setLoading(true); setError('')
    const loader = kind === 'products' ? api.products : kind === 'categories' ? api.categories : kind === 'customers' ? api.customers : api.suppliers
    Promise.all([loader(), kind === 'products' ? api.categories() : Promise.resolve([])])
      .then(([data, cats]) => { setRecords(data); setCategories(cats) })
      .catch((e) => setError(e.message)).finally(() => setLoading(false))
  }, [kind])
  useEffect(load, [load])

  const filtered = useMemo(() => records.filter((record) => Object.values(record).some((v) => String(v ?? '').toLowerCase().includes(query.toLowerCase()))), [records, query])
  const setField = (key, value) => setForm((old) => ({ ...old, [key]: value }))
  const reset = () => { setForm(blank); setEditing(null); setError(''); setNotice('') }
  const startEdit = (record) => {
    setEditing(record.id); setNotice(''); setError('')
    setForm({ ...blank, ...record, price: record.priceCents == null ? '' : moneyInput(record.priceCents), categoryId: record.categoryId || '', minimumStock: String(record.minimumStock ?? 0) })
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  async function submit(event) {
    event.preventDefault(); setSaving(true); setError(''); setNotice('')
    const payload = { ...form }
    if (kind === 'products') {
      payload.priceCents = centsFromInput(form.price); payload.minimumStock = Number(form.minimumStock); payload.categoryId = form.categoryId ? Number(form.categoryId) : null
      delete payload.price
    }
    try {
      if (editing) await api.update(`/api/${settings.route}/${editing}`, payload)
      else await api.create(`/api/${settings.route}`, payload)
      reset(); setNotice(`${settings.singular[0].toUpperCase()}${settings.singular.slice(1)} ${editing ? 'atualizado' : 'cadastrado'} com sucesso.`); load()
    } catch (e) { setError(e.message) } finally { setSaving(false) }
  }

  async function remove(record) {
    const action = kind === 'products' ? 'desativar' : 'excluir'
    if (!window.confirm(`Deseja ${action} “${record.name}”?`)) return
    setError(''); setNotice('')
    try { await api.remove(`/api/${settings.route}/${record.id}`); setNotice(kind === 'products' ? 'Produto desativado.' : 'Registro excluído.'); load() }
    catch (e) { setError(e.message) }
  }

  async function activate(record) {
    setError(''); setNotice('')
    try { await api.activateProduct(record.id); setNotice('Produto ativado.'); load() }
    catch (e) { setError(e.message) }
  }

  async function showHistory(customer) {
    setHistory({ id: customer.id, data: null, loading: true, error: '' })
    try { const data = await api.customerHistory(customer.id); setHistory({ id: customer.id, data, loading: false, error: '' }) }
    catch (e) { setHistory({ id: customer.id, data: [], loading: false, error: e.message }) }
  }

  return <>
    <PageHeading eyebrow="CADASTROS" title={settings.title} detail={`Mantenha ${settings.title.toLowerCase()} e informações operacionais organizados.`} />
    {(error || notice) && <div className={error ? 'alert error' : 'alert success'} role={error ? 'alert' : 'status'}>{error || notice}</div>}
    <section className="content-grid">
      <article className="panel form-panel"><div className="panel-heading"><div><h3>{editing ? `Editar ${settings.singular}` : `Novo ${settings.singular}`}</h3><p>Os campos obrigatórios estão marcados.</p></div></div>
        <form onSubmit={submit} className="form-stack">
          {settings.fields.map((field) => <Field key={field} label={labels[field]}>
            {field === 'description' ? <textarea required={false} maxLength={field === 'description' ? 500 : undefined} value={form[field] || ''} onChange={(e) => setField(field, e.target.value)} rows="3" /> :
              field === 'categoryId' ? <select value={form.categoryId} onChange={(e) => setField(field, e.target.value)}><option value="">Sem categoria</option>{categories.map((category) => <option value={category.id} key={category.id}>{category.name}</option>)}</select> :
                <input required={['code', 'name', 'price', 'minimumStock'].includes(field)} type={field === 'email' ? 'email' : field === 'price' || field === 'minimumStock' ? 'number' : 'text'} min={field === 'price' || field === 'minimumStock' ? 0 : undefined} step={field === 'price' ? '0.01' : undefined} maxLength={field === 'code' ? 40 : undefined} value={form[field] ?? ''} onChange={(e) => setField(field, e.target.value)} />}
          </Field>)}
          {kind === 'products' && <p className="form-hint">O estoque começa em zero e aumenta ao receber uma compra.</p>}
          <div className="form-actions"><button className="button primary" type="submit" disabled={saving}>{saving ? 'Salvando…' : editing ? 'Salvar alterações' : `Cadastrar ${settings.singular}`}</button>{editing && <button className="button secondary" type="button" onClick={reset}>Cancelar edição</button>}</div>
        </form>
      </article>
      <article className="panel list-panel"><div className="panel-heading"><div><h3>{settings.title} cadastrados</h3><p>{records.length} registro(s)</p></div><input className="search-input" aria-label={`Buscar ${settings.title.toLowerCase()}`} placeholder="Buscar…" value={query} onChange={(e) => setQuery(e.target.value)} /></div>
        {loading ? <LoadingState /> : filtered.length === 0 ? <EmptyState title={records.length ? 'Nenhum resultado encontrado' : `Nenhum ${settings.singular} cadastrado`} detail={records.length ? 'Tente ajustar a busca.' : `Use o formulário para cadastrar o primeiro ${settings.singular}.`} /> :
          <div className="record-list">{filtered.map((record) => <div className="record-row" key={record.id}>
            <div className="record-main"><strong>{record.name}</strong><span>{record.code || record.email || record.categoryName || record.description || '—'}</span></div>
            {kind === 'products' && <div className="record-extra"><strong>{formatMoney(record.priceCents)}</strong><span>{record.stock} em estoque {record.active ? '' : '· Inativo'}</span></div>}
            <div className="row-actions">{kind === 'customers' && <button className="text-button" onClick={() => showHistory(record)}>Histórico</button>}<button className="text-button" onClick={() => startEdit(record)}>Editar</button>{kind === 'products' && !record.active ? <button className="text-button" onClick={() => activate(record)}>Ativar</button> : <button className="text-button danger-text" onClick={() => remove(record)}>{kind === 'products' ? 'Desativar' : 'Excluir'}</button>}</div>
          </div>)}</div>}
      </article>
    </section>
    {history && <section className="panel history-panel"><div className="panel-heading"><div><h3>Histórico de compras</h3><p>{records.find((record) => record.id === history.id)?.name}</p></div><button className="text-button" onClick={() => setHistory(null)}>Fechar</button></div>
      {history.loading ? <LoadingState /> : history.error ? <ErrorState message={history.error} /> : history.data.length === 0 ? <EmptyState title="Sem compras registradas" detail="As vendas vinculadas a este cliente aparecerão aqui." /> : <div className="table-wrap"><table><thead><tr><th>Venda</th><th>Data</th><th>Situação</th><th className="numeric">Total</th></tr></thead><tbody>{history.data.map((sale) => <tr key={sale.id}><td>#{sale.id}</td><td>{new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(sale.createdAt))}</td><td>{sale.status === 'CONFIRMADA' ? 'Confirmada' : 'Cancelada'}</td><td className="numeric">{formatMoney(sale.totalCents)}</td></tr>)}</tbody></table></div>}
    </section>}
  </>
}
