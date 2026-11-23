export function LoadingState() {
  return <div className="page-state" role="status"><span className="spinner" />Carregando dados…</div>
}

export function ErrorState({ message, onRetry }) {
  return <div className="alert error" role="alert"><span>{message}</span>{onRetry && <button className="text-button" onClick={onRetry}>Tentar novamente</button>}</div>
}

export function EmptyState({ title, detail }) {
  return <div className="empty-state"><strong>{title}</strong><span>{detail}</span></div>
}

import { cloneElement } from 'react'

export function Field({ label, children, hint, ...props }) {
  const id = props.id || `field-${label.toLowerCase().replace(/[^a-z0-9]+/g, '-')}`
  return <label className="field" htmlFor={id}>
    <span>{label}</span>
    {typeof children === 'function' ? children({ id }) : cloneElement(children, { id })}
    {hint && <small>{hint}</small>}
  </label>
}

export function PageHeading({ eyebrow, title, detail, action }) {
  return <div className="page-heading"><div><span className="eyebrow">{eyebrow}</span><h2>{title}</h2>{detail && <p>{detail}</p>}</div>{action}</div>
}

export function StatusBadge({ status }) {
  const state = (status || '').toLowerCase()
  return <span className={`badge ${state}`}>
    {({ confirmada: 'Confirmada', cancelada: 'Cancelada', pendente: 'Pendente', parcial: 'Parcial', paga: 'Paga', vencida: 'Vencida', recebida: 'Recebida' })[state] || status}
  </span>
}
