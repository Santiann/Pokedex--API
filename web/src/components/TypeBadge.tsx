import { typeColor, typeLabel } from '../i18n'

export function TypeBadge({ type }: { type: string }) {
  return (
    <span className="type-badge" style={{ '--badge': typeColor(type) } as React.CSSProperties}>
      {typeLabel(type)}
    </span>
  )
}
