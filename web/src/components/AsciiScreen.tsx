import { memo, useMemo } from 'react'
import { artSize, shadeArt } from '../ascii'

type Props = { art: string; color: string }

/** A arte ASCII original, ajustada à tela e revelada como numa varredura de CRT. */
export const AsciiScreen = memo(function AsciiScreen({ art, color }: Props) {
  const lines = useMemo(() => shadeArt(art, color), [art, color])
  const { cols, rows } = useMemo(() => artSize(art), [art])

  return (
    <pre
      className="ascii"
      style={{ '--cols': cols, '--rows': rows } as React.CSSProperties}
      aria-hidden="true"
    >
      {lines.map((runs, i) => (
        <span key={i} className="block">
          {runs.map((run, j) => (
            <span key={j} style={{ color: run.color }}>
              {run.text}
            </span>
          ))}
        </span>
      ))}
    </pre>
  )
})
