import { useEffect, useState } from 'react'
import { api, formatDate, formatDateTime, formatMoney } from '../services/api.js'
import { EmptyState, ErrorState, Field, LoadingState, PageHeading, StatusBadge } from '../components/PageState.jsx'

const today = () => { const d = new Date(); return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}` }
const monthStart = () => `${today().slice(0, 7)}-01`

export default function ReportsPage() {
  const [start, setStart] = useState(monthStart())
  const [end, setEnd] = useState(today())
  const [report, setReport] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const load = () => {
    setLoading(true); setError('')
    api.report(start, end).then(setReport).catch((e) => setError(e.message)).finally(() => setLoading(false))
  }
  useEffect(load, [])
  const lowStock = report?.stock.filter((p) => p.active && p.stock <= p.minimumStock) || []
  const maxSales = Math.max(1, ...(report?.bestSellers.map((p) => p.quantitySold) || []))
  return <>
    <PageHeading eyebrow="ANÁLISE" title="Relatórios" detail="Resumo de vendas, compras, financeiro e estoque no período selecionado." />
    {error && <ErrorState message={error} onRetry={load} />}
    <section className="panel report-filter"><div className="date-fields"><Field label="De"><input type="date" required value={start} onChange={(e) => setStart(e.target.value)} /></Field><Field label="Até"><input type="date" required value={end} onChange={(e) => setEnd(e.target.value)} /></Field><button className="button primary" onClick={load} disabled={loading}>{loading ? 'Atualizando…' : 'Atualizar relatório'}</button></div><span className="form-hint">O período máximo é de 367 dias. Datas e valores seguem o horário local do servidor.</span></section>
    {loading ? <LoadingState /> : report && <>
      <section className="metrics report-metrics"><ReportMetric label="Vendas confirmadas" value={report.salesCount} /><ReportMetric label="Total vendido" value={formatMoney(report.salesTotalCents)} /><ReportMetric label="Recebido no período" value={formatMoney(report.receivablesPaidCents)} /><ReportMetric label="Pago no período" value={formatMoney(report.payablesPaidCents)} /></section>
      <section className="report-grid">
        <article className="panel"><div className="panel-heading"><div><h3>Produtos mais vendidos</h3><p>Quantidade vendida no período</p></div></div>
          {report.bestSellers.length === 0 ? <EmptyState title="Sem vendas no período" detail="Produtos vendidos aparecerão neste relatório." /> : <div className="bar-list">{report.bestSellers.map((p) => <div className="bar-row" key={p.name}><div className="bar-label"><strong>{p.name}</strong><span>{p.quantitySold} un. · {formatMoney(p.totalCents)}</span></div><div className="bar-track"><span style={{ width: `${Math.max(4, p.quantitySold / maxSales * 100)}%` }} /></div></div>)}</div>}
        </article>
        <article className="panel"><div className="panel-heading"><div><h3>Estoque baixo</h3><p>Saldo atual em comparação ao mínimo</p></div></div>
          {lowStock.length === 0 ? <EmptyState title="Nenhum alerta de estoque" detail="Produtos no mínimo ou abaixo aparecerão aqui." /> : <div className="record-list">{lowStock.map((p) => <div className="record-row" key={p.id}><div className="record-main"><strong>{p.name}</strong><span>{p.code}</span></div><div className="record-extra"><strong>{p.stock} em estoque</strong><span>Mínimo: {p.minimumStock}</span></div></div>)}</div>}
        </article>
      </section>
      <article className="panel section-gap"><div className="panel-heading"><div><h3>Vendas do período</h3><p>{formatDate(report.startDate)} a {formatDate(report.endDate)}</p></div></div>
        {report.sales.length === 0 ? <EmptyState title="Sem vendas neste período" detail="Nenhuma venda foi registrada nas datas selecionadas." /> : <div className="table-wrap"><table><thead><tr><th>Venda</th><th>Cliente</th><th>Data</th><th>Situação</th><th className="numeric">Total</th></tr></thead><tbody>{report.sales.map((s) => <tr key={s.id}><td>#{s.id}</td><td>{s.customerName}</td><td>{formatDateTime(s.createdAt)}</td><td><StatusBadge status={s.status} /></td><td className="numeric">{formatMoney(s.totalCents)}</td></tr>)}</tbody></table></div>}
      </article>
      <article className="panel section-gap"><div className="panel-heading"><div><h3>Compras do período</h3><p>Pedidos registrados</p></div></div>
        {report.purchases.length === 0 ? <EmptyState title="Sem compras neste período" detail="Nenhuma compra foi registrada nas datas selecionadas." /> : <div className="table-wrap"><table><thead><tr><th>Compra</th><th>Fornecedor</th><th>Data</th><th>Situação</th><th className="numeric">Total</th></tr></thead><tbody>{report.purchases.map((p) => <tr key={p.id}><td>#{p.id}</td><td>{p.supplierName}</td><td>{formatDateTime(p.createdAt)}</td><td><StatusBadge status={p.status} /></td><td className="numeric">{formatMoney(p.totalCents)}</td></tr>)}</tbody></table></div>}
      </article>
    </>}
  </>
}

function ReportMetric({ label, value }) { return <article className="metric-card"><span className="metric-label">{label}</span><strong>{value}</strong></article> }
