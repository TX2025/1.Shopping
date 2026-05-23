<template>
  <nav class="mega-menu" @mouseleave="activeIndex = null">
    <div class="mega-menu-inner">
      <div
        v-for="(cat, idx) in topCategories"
        :key="cat.id"
        class="mega-menu-item"
        :class="{ active: activeIndex === idx }"
        @mouseenter="activeIndex = idx"
        @click="goCategory(cat)"
      >
        <span class="mega-menu-label">{{ cat.name }}</span>
        <el-icon v-if="cat.children?.length" :size="12" class="arrow"><ArrowDown /></el-icon>
      </div>
    </div>
    <div class="mega-dropdown" v-if="activeIndex !== null && currentChildren.length" @mouseleave="activeIndex = null">
      <div class="dropdown-inner">
        <div class="dropdown-categories">
          <div
            v-for="child in currentChildren"
            :key="child.id"
            class="dropdown-cat"
            @click="goCategory(child)"
          >
            <div class="dropdown-cat-icon">
              <el-icon :size="28"><FolderOpened /></el-icon>
            </div>
            <span class="dropdown-cat-name">{{ child.name }}</span>
          </div>
        </div>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCategories } from '../api/product'
import { ArrowDown, FolderOpened } from '@element-plus/icons-vue'

const router = useRouter()
const topCategories = ref([])
const activeIndex = ref(null)

const currentChildren = computed(() => {
  if (activeIndex.value === null) return []
  const cat = topCategories.value[activeIndex.value]
  return cat?.children || []
})

function goCategory(cat) {
  activeIndex.value = null
  router.push(`/products?categoryId=${cat.id}`)
}

onMounted(async () => {
  try {
    const res = await getCategories()
    topCategories.value = (res.data || []).slice(0, 7)
  } catch {}
})
</script>

<style scoped>
.mega-menu {
  background: #00676b;
  position: relative;
  z-index: 99;
}
.mega-menu-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  height: 42px;
  gap: 2px;
}
.mega-menu-item {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 10px 18px;
  color: rgba(255,255,255,0.92);
  font-size: 14px;
  cursor: pointer;
  border-radius: 2px;
  transition: background 0.2s;
  white-space: nowrap;
  user-select: none;
}
.mega-menu-item:hover,
.mega-menu-item.active {
  background: rgba(255,255,255,0.16);
  color: #fff;
}
.arrow {
  transition: transform 0.2s;
}
.mega-menu-item.active .arrow {
  transform: rotate(180deg);
}

.mega-dropdown {
  position: absolute;
  left: 0;
  right: 0;
  top: 100%;
  background: #fff;
  box-shadow: 0 12px 32px rgba(0,0,0,0.12);
  animation: fadeIn 0.15s ease;
}
.dropdown-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 20px;
}
.dropdown-categories {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 12px;
}
.dropdown-cat {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  color: #333;
}
.dropdown-cat:hover {
  background: #f0f7f7;
  color: #00676b;
}
.dropdown-cat-icon {
  color: #00676b;
  flex-shrink: 0;
}
.dropdown-cat-name {
  font-size: 14px;
  font-weight: 500;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-4px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 768px) {
  .mega-menu-inner {
    overflow-x: auto;
    gap: 0;
  }
  .mega-menu-item {
    padding: 10px 12px;
    font-size: 13px;
  }
  .dropdown-categories {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
