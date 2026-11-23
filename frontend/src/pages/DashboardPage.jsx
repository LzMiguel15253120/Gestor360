import { useEffect, useState } from 'react'
import { api, formatDateTime, formatMoney } from '../services/api.js'
import { EmptyState, ErrorState, LoadingState, PageHeading } from '../components/PageState.jsx'

export default function DashboardPage() {
  const [data, setData] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const load = () => {
    setLoading(true)
    api.dashboard().then(setData).catch((e) => setError(e.message)).finally(() => setLoading(false))
  }
  useEffect(load, [])

  return <>
    <PageHeading eyebrow="VISÃO GERAL" title="Painel" detail="Resumo atualizado com os dados registrados no sistema." />
    {error && <ErrorState message={error} onRetry={load} />}
    {loading ? <LoadingState /> : data && <>
      <section className="metrics" aria-label="Indicadores do dia">
        <Metric label="Vendas de hoje" value={formatMoney(data.salesTodayCents)} icon="↗" />
        <Metric label="Recebido hoje" value={formatMoney(data.receivedTodayCents)} icon="↓" />
        <Metric label="Pago hoje" value={formatMoney(data.paidTodayCents)} icon="↑" />
        <Metric label="Estoque baixo" value={data.lowStockCount} icon="!" />
      </section>
      <section className="dashboard-grid">
        <article className="panel"><div className="panel-heading"><div><h3>Vendas recentes</h3><p>Últimas movimentações confirmadas</p></div></div>
          {data.recentSales.length === 0 ? <EmptyState title="Nenhuma venda registrada" detail="As vendas confirmadas aparecerão aqui." /> :
            <div className="table-wrap"><table><thead><tr><th>Venda</th><th>Cliente</th><th>Data</th><th>Situação</th><th className="numeric">Total</th></tr></thead><tbody>
              {data.recentSales.map((sale) => <tr key={sale.id}><td className="strong">#{sale.id}</td><td>{sale.customerName}</td><td>{formatDateTime(sale.createdAt)}</td><td>{sale.status === 'CONFIRMADA' ? 'Confirmada' : 'Cancelada'}</td><td className="numeric strong">{formatMoney(sale.totalCents)}</td></tr>)}
            </tbody></table></div>}
        </article>
        <article className="panel finance-summary"><div className="panel-heading"><div><h3>Financeiro em aberto</h3><p>Saldo pendente das contas</p></div></div>
          <div className="balance-line"><span>A receber</span><strong>{formatMoney(data.pendingReceivablesCents)}</strong></div>
          <div className="balance-line"><span>A pagar</span><strong>{formatMoney(data.pendingPayablesCents)}</strong></div>
          <p className="muted-note">Os valores consideram pagamentos parciais já registrados.</p>
        </article>
      </section>
    </>}
  </>
}

function Metric({ label, value, icon }) {
  return <article className="metric-card"><span className="metric-icon" aria-hidden="true">{icon}</span><span className="metric-label">{label}</span><strong>{value}</strong></article>
}
