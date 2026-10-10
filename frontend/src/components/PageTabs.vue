<template>
  <div class="page-tabs">
    <div class="tabs-track">
      <button
        v-for="tab in tabsStore.tabs"
        :key="tab.path"
        type="button"
        class="tab-item"
        :class="{ active: activePath === tab.path, pinned: !tab.closable }"
        @click="switchTab(tab.path)"
      >
        <i class="tab-dot" aria-hidden="true" />
        <span class="tab-title">{{ tab.title }}</span>
        <span
          v-if="tab.closable"
          class="tab-close"
          role="button"
          tabindex="-1"
          title="关闭"
          @click.stop="closeTab(tab.path)"
        >
          <el-icon><Close /></el-icon>
        </span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useTabsStore } from '@/stores/tabs'

const route = useRoute()
const router = useRouter()
const tabsStore = useTabsStore()
const refreshView = inject('refreshView', null)

const activePath = computed(() => (route.path === '/admin' ? '/admin/home' : route.path))

function switchTab(path) {
  if (activePath.value === path) {
    refreshView?.()
    return
  }
  router.push(path)
}

function closeTab(path) {
  tabsStore.closeTab(path, router)
}
</script>

<style scoped>
.page-tabs {
  position: relative;
  padding: 10px 18px 8px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.72) 0%, rgba(248, 250, 252, 0.92) 100%),
    var(--app-page-bg, #f5f7fa);
  border-bottom: 1px solid rgba(15, 23, 42, 0.06);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.85);
}

.page-tabs::after {
  content: '';
  position: absolute;
  left: 18px;
  right: 18px;
  bottom: 0;
  height: 1px;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(15, 23, 42, 0.06) 12%,
    rgba(15, 23, 42, 0.06) 88%,
    transparent
  );
  pointer-events: none;
}

.tabs-track {
  display: flex;
  align-items: center;
  gap: 8px;
  overflow-x: auto;
  padding-bottom: 2px;
  scrollbar-width: thin;
  scrollbar-color: rgba(148, 163, 184, 0.35) transparent;
}

.tabs-track::-webkit-scrollbar {
  height: 4px;
}

.tabs-track::-webkit-scrollbar-thumb {
  background: rgba(148, 163, 184, 0.35);
  border-radius: 999px;
}

.tabs-track::-webkit-scrollbar-track {
  background: transparent;
}

.tab-item {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  height: 34px;
  padding: 0 12px 0 10px;
  border: 1px solid rgba(15, 23, 42, 0.06);
  border-radius: 10px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.92) 0%, rgba(248, 250, 252, 0.88) 100%);
  color: #64748b;
  font-size: 13px;
  font-weight: 550;
  letter-spacing: 0.01em;
  cursor: pointer;
  white-space: nowrap;
  user-select: none;
  font-family: inherit;
  box-shadow:
    0 1px 2px rgba(15, 23, 42, 0.03),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
  transition:
    color 0.18s ease,
    background 0.18s ease,
    border-color 0.18s ease,
    box-shadow 0.18s ease,
    transform 0.18s ease;
}

.tab-item:hover {
  color: #334155;
  border-color: rgba(15, 23, 42, 0.1);
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  box-shadow:
    0 6px 14px rgba(15, 23, 42, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.95);
  transform: translateY(-1px);
}

.tab-item.active {
  color: var(--app-primary);
  border-color: color-mix(in srgb, var(--app-primary) 28%, transparent);
  background:
    linear-gradient(
      180deg,
      #ffffff 0%,
      color-mix(in srgb, var(--app-primary) 6%, #ffffff) 100%
    );
  box-shadow:
    0 8px 18px color-mix(in srgb, var(--app-primary) 12%, transparent),
    0 1px 2px rgba(15, 23, 42, 0.04),
    inset 0 1px 0 rgba(255, 255, 255, 0.95);
  font-weight: 680;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  left: 10px;
  right: 10px;
  bottom: -1px;
  height: 2px;
  border-radius: 999px;
  background: linear-gradient(
    90deg,
    transparent,
    var(--app-primary),
    transparent
  );
  opacity: 0.9;
}

.tab-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
  background: #cbd5e1;
  box-shadow: inset 0 1px 1px rgba(255, 255, 255, 0.55);
  transition: background 0.18s ease, box-shadow 0.18s ease, transform 0.18s ease;
}

.tab-item:hover .tab-dot {
  background: #94a3b8;
}

.tab-item.active .tab-dot {
  background: var(--app-primary);
  box-shadow:
    0 0 0 3px color-mix(in srgb, var(--app-primary) 18%, transparent),
    inset 0 1px 1px rgba(255, 255, 255, 0.45);
  transform: scale(1.05);
}

.tab-title {
  line-height: 1;
}

.tab-close {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  margin-left: 2px;
  margin-right: -2px;
  border-radius: 6px;
  color: #94a3b8;
  opacity: 0.55;
  transition: opacity 0.16s ease, color 0.16s ease, background 0.16s ease;
}

.tab-item:hover .tab-close,
.tab-item.active .tab-close {
  opacity: 1;
}

.tab-close:hover {
  color: #ef4444;
  background: rgba(239, 68, 68, 0.1);
}

.tab-close :deep(.el-icon) {
  font-size: 12px;
}

.tab-item.pinned {
  padding-right: 14px;
}
</style>
