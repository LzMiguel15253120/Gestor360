import { useCallback, useEffect, useState } from 'react'
import { api, formatDateTime } from '../services/api.js'
import { EmptyState, ErrorState, LoadingState, PageHeading } from '../components/PageState.jsx'

export default function StockPage() {
  const [products, setProducts] = useState([])
  const [movements, setMovements] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const load = useCallback(() => {
    setLoading(true); setError('')
    Promise.all([api.products(), api.movements()]).then(([p, m]) => { setProducts(p); setMovements(m) }).catch((e) => setError(e.message)).finally(() => setLoading(false))
  }, [])
  useEffect(load, [load])
  const lowStock = products.filter((p) => p.active && p.stock <= p.minimumStock)
  return <>
    <PageHeading eyebrow="CONTROLE" title="Estoque" detail="Saldo atual e histórico de entradas e saídas registradas." />
    {error && <ErrorState message={error} onRetry={load} />}
    {loading ? <LoadingState /> : <>
      <article className="panel"><div className="panel-heading"><div><h3>Posição de estoque</h3><p>{products.filter((p) => p.active).length} produto(s) ativo(s) · {lowStock.length} abaixo ou no mínimo</p></div></div>
        {products.length === 0 ? <EmptyState title="Nenhum produto cadastrado" detail="Cadastre produtos antes de acompanhar o estoque." /> : <div className="table-wrap"><table><thead><tr><th>Produto</th><th>Código</th><th>Categoria</th><th className="numeric">Disponível</th><th className="numeric">Mínimo</th><th>Situação</th></tr></thead><tbody>
          {products.map((p) => <tr key={p.id}><td className="strong">{p.name}</td><td>{p.code}</td><td>{p.categoryName || '—'}</td><td className="numeric">{p.stock}</td><td className="numeric">{p.minimumStock}</td><td>{!p.active ? <span className="badge cancelada">Inativo</span> : p.stock <= p.minimumStock ? <span className="badge vencida">Estoque baixo</span> : <span className="badge recebida">Normal</span>}</td></tr>)}
        </tbody></table></div>}
      </article>
      <article className="panel section-gap"><div className="panel-heading"><div><h3>Movimentações recentes</h3><p>Até 100 eventos mais recentes</p></div></div>
        {movements.length === 0 ? <EmptyState title="Nenhuma movimentação registrada" detail="Recebimentos e vendas criarão o histórico automaticamente." /> : <div className="table-wrap"><table><thead><tr><th>Data</th><th>Produto</th><th>Movimento</th><th>Referência</th><th className="numeric">Variação</th></tr></thead><tbody>{movements.map((m) => <tr key={m.id}><td>{formatDateTime(m.createdAt)}</td><td>{m.productName}</td><td>{({ SAIDA_VENDA: 'Venda', ENTRADA_COMPRA: 'Recebimento', ESTORNO_VENDA: 'Devolução' })[m.type] || m.type}</td><td>{m.referenceType === 'VENDA' ? 'Venda' : 'Compra'} #{m.referenceId}</td><td className={`numeric strong ${m.quantityDelta > 0 ? 'positive' : 'negative'}`}>{m.quantityDelta > 0 ? '+' : ''}{m.quantityDelta}</td></tr>)}</tbody></table></div>}
      </article>
    </>}
  </>
}
