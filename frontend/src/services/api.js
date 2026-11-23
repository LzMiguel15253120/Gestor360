async function request(path, options = {}) {
  let response
  try {
    response = await fetch(path, {
      ...options,
      headers: { ...(options.body ? { 'Content-Type': 'application/json' } : {}), ...options.headers },
    })
  } catch {
    throw new Error('Não foi possível conectar à API. Confira se o backend está iniciado.')
  }

  if (response.status === 204) return null
  const data = await response.json().catch(() => ({}))
  if (!response.ok) throw new Error(data.message || 'Não foi possível concluir a operação.')
  return data
}

const json = (method, value) => ({ method, body: JSON.stringify(value) })

export const api = {
  health: () => request('/api/health'),
  dashboard: () => request('/api/dashboard'),
  report: (start, end) => request(`/api/relatorios?inicio=${encodeURIComponent(start)}&fim=${encodeURIComponent(end)}`),
  products: () => request('/api/produtos'),
  categories: () => request('/api/categorias'),
  customers: () => request('/api/clientes'),
  customerHistory: (id) => request(`/api/clientes/${id}/compras`),
  suppliers: () => request('/api/fornecedores'),
  sales: () => request('/api/vendas'),
  purchases: () => request('/api/compras'),
  movements: () => request('/api/estoque/movimentacoes'),
  accounts: (type = '') => request(`/api/financeiro/contas${type ? `?tipo=${encodeURIComponent(type)}` : ''}`),
  create: (path, value) => request(path, json('POST', value)),
  update: (path, value) => request(path, json('PUT', value)),
  remove: (path) => request(path, { method: 'DELETE' }),
  cancelSale: (id) => request(`/api/vendas/${id}/cancelar`, { method: 'POST' }),
  activateProduct: (id) => request(`/api/produtos/${id}/ativar`, { method: 'POST' }),
  receivePurchase: (id) => request(`/api/compras/${id}/receber`, { method: 'POST' }),
  cancelPurchase: (id) => request(`/api/compras/${id}/cancelar`, { method: 'POST' }),
  payments: (accountId) => request(`/api/financeiro/contas/${accountId}/pagamentos`),
  registerPayment: (accountId, payment) => request(`/api/financeiro/contas/${accountId}/pagamentos`, json('POST', payment)),
}

export const formatMoney = (cents = 0) => new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(cents / 100)
export const formatDate = (value) => value ? new Intl.DateTimeFormat('pt-BR').format(new Date(value.length === 10 ? `${value}T12:00:00` : value)) : '—'
export const formatDateTime = (value) => value ? new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(value)) : '—'
export const centsFromInput = (value) => Math.round(Number(String(value).replace(',', '.')) * 100)
export const moneyInput = (cents) => (cents / 100).toFixed(2)
