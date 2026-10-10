<template>
  <span class="brand-logo-wrap" :class="[`size-${size}`, { 'is-image': hasCustomLogo }]">
    <img v-if="hasCustomLogo" class="brand-logo-img" :src="logoSrc" alt="logo" />
    <svg
      v-else
      class="brand-logo-svg"
      viewBox="0 0 40 40"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      aria-hidden="true"
    >
      <circle cx="20" cy="20" r="20" fill="#e63935" />
      <path
        d="M20 8.2L30.8 14.4V25.6L20 31.8L9.2 25.6V14.4L20 8.2Z"
        stroke="#fff"
        stroke-width="1.7"
        stroke-linejoin="round"
        fill="rgba(255,255,255,0.1)"
      />
      <rect x="13.2" y="21" width="3.4" height="5.6" rx="1" fill="#fff" />
      <rect x="18.3" y="17.6" width="3.4" height="9" rx="1" fill="#fff" fill-opacity=".92" />
      <rect x="23.4" y="14.4" width="3.4" height="12.2" rx="1" fill="#fff" fill-opacity=".84" />
    </svg>
  </span>
</template>

<script setup>
import { storeToRefs } from 'pinia'
import { useSystemStore } from '@/stores/system'

defineProps({
  size: {
    type: String,
    default: 'md' // sm | md | lg
  }
})

const systemStore = useSystemStore()
const { logoSrc, hasCustomLogo } = storeToRefs(systemStore)
</script>

<style scoped>
.brand-logo-wrap {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
}

.brand-logo-wrap:not(.is-image) {
  border-radius: 50%;
}

.brand-logo-wrap.is-image {
  border-radius: 10px;
}

.size-sm {
  width: 32px;
  height: 32px;
}

.size-md {
  width: 42px;
  height: 42px;
}

.size-lg {
  width: 48px;
  height: 48px;
  border-radius: 12px;
}

.brand-logo-img,
.brand-logo-svg {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
}

.brand-logo-wrap.is-image .brand-logo-img {
  border-radius: inherit;
}
</style>
