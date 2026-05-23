import { getPageConfig } from '../api/admin'
import { ref } from 'vue'

const cache = {}

export function usePageConfig(pageType) {
  const config = ref({})
  const loading = ref(false)

  async function load() {
    if (cache[pageType]) {
      config.value = cache[pageType]
      return
    }
    loading.value = true
    try {
      const res = await getPageConfig(pageType)
      const pc = res.data
      const json = pc?.configJson ? JSON.parse(pc.configJson) : {}
      cache[pageType] = json
      config.value = json
    } catch {
      config.value = {}
    } finally {
      loading.value = false
    }
  }

  load()
  return { config, loading, reload: load }
}
