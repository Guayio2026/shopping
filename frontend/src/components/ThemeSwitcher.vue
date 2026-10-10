<template>
  <el-dropdown trigger="click" @command="themeStore.setTheme" popper-class="theme-switcher-popper">
    <div class="toolbar-item">
      <el-icon :size="15"><Brush /></el-icon>
      <span>主题</span>
    </div>
    <template #dropdown>
      <el-dropdown-menu class="theme-dropdown">
        <el-dropdown-item
          v-for="item in THEME_PRESETS"
          :key="item.key"
          :command="item.key"
          :class="{ active: themeStore.currentKey === item.key }"
        >
          <span class="color-dot" :style="{ background: item.active, boxShadow: `0 0 0 1px ${item.shell}` }" />
          <span class="theme-name">{{ item.label }}</span>
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup>
import { useThemeStore, THEME_PRESETS } from '@/stores/theme'

const themeStore = useThemeStore()
</script>

<style scoped>
.color-dot {
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 8px;
  vertical-align: -1px;
  flex-shrink: 0;
}

.theme-name {
  flex: 1;
}

:deep(.theme-dropdown .el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  color: #334155;
  min-width: 168px;
}

:deep(.theme-dropdown .el-dropdown-menu__item:hover),
:deep(.theme-dropdown .el-dropdown-menu__item:focus) {
  color: var(--app-primary);
  background: var(--app-primary-soft);
}

:deep(.theme-dropdown .active) {
  color: var(--app-primary);
  background: var(--app-primary-soft);
  font-weight: 600;
}
</style>
