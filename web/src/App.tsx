import { useQuery } from '@tanstack/react-query'
import { useEffect, useState } from 'react'
import { pokemonListQuery } from './api'
import { Device } from './components/Device'
import { PokemonIndex } from './components/PokemonIndex'
import { useSelectedId } from './useSelectedId'

export default function App() {
  const [id, select] = useSelectedId()
  const list = useQuery(pokemonListQuery)

  useEffect(() => {
    const name = list.data?.[id - 1]?.displayName
    document.title = name ? `${name} · Pokédex ASCII` : 'Pokédex ASCII'
  }, [id, list.data])

  // ← e → navegam pela Pokédex quando ninguém está digitando.
  useEffect(() => {
    const onKey = (event: KeyboardEvent) => {
      if (event.target instanceof HTMLInputElement || event.metaKey || event.ctrlKey || event.altKey) return
      if (event.key === 'ArrowLeft') select(id - 1)
      if (event.key === 'ArrowRight') select(id + 1)
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [id, select])

  const selectAndReveal = (next: number) => {
    select(next)
    if (window.matchMedia('(max-width: 959px)').matches) {
      window.scrollTo({ top: 0, behavior: 'smooth' })
    }
  }

  return (
    <div className="page">
      <aside className="page-device">
        <Device id={id} onSelect={select} />
      </aside>

      <main className="page-main">
        <header className="intro">
          <p className="eyebrow">Geração I · 151 artes ASCII</p>
          <h1>
            Feita à mão,
            <br />
            <em>caractere por caractere.</em>
          </h1>
          <p className="lede">
            Meu primeiro projeto programando sozinho, na faculdade em 2022: uma Pokédex de terminal em Java, com uma arte ASCII
            para cada um dos 151 originais. Hoje ela também tem uma API em Spring Boot e esta interface. As artes continuam as mesmas.
          </p>
          <CurlHint id={id} />
        </header>

        {list.isPending && <p className="index-empty blink">Carregando os 151…</p>}
        {list.isError && (
          <p className="index-empty">
            Não consegui falar com a PokeAPI agora.{' '}
            <button type="button" className="link" onClick={() => list.refetch()}>
              Tentar de novo
            </button>
          </p>
        )}
        {list.data && <PokemonIndex pokemon={list.data} selectedId={id} onSelect={selectAndReveal} />}

        <footer className="footer">
          <a href="/docs">Documentação da API</a>
          <a href="https://github.com/Santiann/Pokedex--API">Código no GitHub</a>
          <span>
            Dados da <a href="https://pokeapi.co">PokeAPI</a>. Pokémon é marca da Nintendo, Game Freak e Creatures.
          </span>
        </footer>
      </main>
    </div>
  )
}

function CurlHint({ id }: { id: number }) {
  const command = `curl ${window.location.origin}/api/pokemon/${id}/ascii`
  const [copied, setCopied] = useState(false)

  const copy = async () => {
    try {
      await navigator.clipboard.writeText(command)
      setCopied(true)
      setTimeout(() => setCopied(false), 1500)
    } catch {
      // Sem permissão de área de transferência: o comando continua visível para copiar à mão.
    }
  }

  return (
    <div className="curl">
      <span className="curl-label">Também funciona no terminal</span>
      <code>
        <span className="curl-prompt">$</span> {command}
      </code>
      <button type="button" onClick={copy}>
        {copied ? 'Copiado' : 'Copiar'}
      </button>
    </div>
  )
}
