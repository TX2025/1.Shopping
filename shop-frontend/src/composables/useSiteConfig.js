import { getSiteConfig } from '../api/admin'
import { ref, onMounted } from 'vue'

const configCache = ref({})
let loaded = false
let pending = null

export function useSiteConfig() {
  async function load() {
    if (loaded) return
    if (pending) return pending
    pending = getSiteConfig().then(res => {
      configCache.value = res.data || {}
      loaded = true
    }).catch(() => {
      configCache.value = {}
    }).finally(() => { pending = null })
    return pending
  }

  function refresh() {
    loaded = false
    pending = null
    return load()
  }

  // auto-load on first use
  if (!loaded && !pending) {
    load()
  }

  return { config: configCache, load, refresh }
}
