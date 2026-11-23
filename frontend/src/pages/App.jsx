import { useEffect, useState } from 'react'
import { api } from '../services/api.js'
import DashboardPage from './DashboardPage.jsx'
import CatalogPage from './CatalogPage.jsx'
import SalesPage from './SalesPage.jsx'
import PurchasesPage from './PurchasesPage.jsx'
import StockPage from './StockPage.jsx'
import FinancePage from './FinancePage.jsx'
import ReportsPage from './ReportsPage.jsx'

const navGroups = [
  { label: 'VISÃO GERAL', items: [{ id: 'dashboard', title: 'Painel', icon: '▦' }] },
  { label: 'CADASTROS', items: [
    { id: 'products', title: 'Produtos', icon: '□' }, { id: 'categories', title: 'Categorias', icon: '◫' },
    { id: 'customers', title: 'Clientes', icon: '♙' }, { id: 'suppliers', title: 'Fornecedores', icon: '◇' },
  ] },
  { label: 'OPERAÇÃO', items: [{ id: 'sales', title: 'Vendas', icon: '↗' }, { id: 'purchases', title: 'Compras', icon: '↙' }, { id: 'stock', title: 'Estoque', icon: '▤' }, { id: 'finance', title: 'Financeiro', icon: '＄' }, { id: 'reports', title: 'Relatórios', icon: '▥' }] },
]
const titles = Object.fromEntries(navGroups.flatMap((group) => group.items.map((item) => [item.id, item.title])))

export default function App() {
  const [active, setActive] = useState('dashboard')
  const [menuOpen, setMenuOpen] = useState(false)
  const [apiStatus, setApiStatus] = useState('verificando')
  useEffect(() => { api.health().then(() => setApiStatus('conectada')).catch(() => setApiStatus('indisponível')) }, [])
  const select = (id) => { setActive(id); setMenuOpen(false) }
  let page
  if (active === 'dashboard') page = <DashboardPage />
  else if (['products', 'categories', 'customers', 'suppliers'].includes(active)) page = <CatalogPage key={active} kind={active} />
  else if (active === 'sales') page = <SalesPage />
  else if (active === 'purchases') page = <PurchasesPage />
  else if (active === 'stock') page = <StockPage />
  else if (active === 'finance') page = <FinancePage />
  else page = <ReportsPage />

  return <div className="app-shell">
    {menuOpen && <button className="mobile-scrim" aria-label="Fechar menu" onClick={() => setMenuOpen(false)} />}
    <aside className={`sidebar ${menuOpen ? 'open' : ''}`}>
      <div className="brand"><div className="brand-mark">G<span>360</span></div><div><strong>Gestor360</strong><small>GESTÃO EMPRESARIAL</small></div></div>
      <nav aria-label="Navegação principal">{navGroups.map((group) => <div className="nav-group" key={group.label}><span className="nav-caption">{group.label}</span>{group.items.map((item) => <button key={item.id} className={`nav-item ${active === item.id ? 'selected' : ''}`} aria-current={active === item.id ? 'page' : undefined} onClick={() => select(item.id)}><span className="nav-icon" aria-hidden="true">{item.icon}</span>{item.title}</button>)}</div>)}</nav>
      <div className="sidebar-bottom"><span className={`api-indicator ${apiStatus}`} /><span>API {apiStatus === 'conectada' ? 'conectada' : apiStatus === 'indisponível' ? 'indisponível' : 'verificando'}</span></div>
    </aside>
    <main className="main-area">
      <header className="topbar"><button className="menu-toggle" aria-label="Abrir navegação" aria-expanded={menuOpen} onClick={() => setMenuOpen((open) => !open)}>☰</button><div className="breadcrumb">Gestor360 <span>/</span> <strong>{titles[active]}</strong></div><div className="topbar-right"><span className="today-label">{new Intl.DateTimeFormat('pt-BR', { dateStyle: 'full' }).format(new Date())}</span><div className="user-chip" aria-label="Sessão local sem autenticação configurada">G</div></div></header>
      <div className="page-content">{page}<footer className="page-footer">Gestor360 <span>·</span> Dados armazenados localmente no banco configurado</footer></div>
    </main>
  </div>
}
