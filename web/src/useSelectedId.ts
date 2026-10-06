import { useCallback, useEffect, useState } from 'react'
import { TOTAL } from './api'

const DEFAULT_ID = 25

function idFromPath(path: string) {
  const match = path.match(/^\/pokemon\/(\d{1,3})\/?$/)
  const id = match ? Number(match[1]) : DEFAULT_ID
  return id >= 1 && id <= TOTAL ? id : DEFAULT_ID
}

/** O Pokémon selecionado vive na URL (/pokemon/25), então dá para compartilhar o link. */
export function useSelectedId() {
  const [id, setId] = useState(() => idFromPath(window.location.pathname))

  useEffect(() => {
    const onPop = () => setId(idFromPath(window.location.pathname))
    window.addEventListener('popstate', onPop)
    return () => window.removeEventListener('popstate', onPop)
  }, [])

  const select = useCallback((next: number) => {
    const wrapped = ((next - 1 + TOTAL) % TOTAL) + 1
    setId(wrapped)
    window.history.pushState(null, '', `/pokemon/${wrapped}`)
  }, [])

  return [id, select] as const
}
