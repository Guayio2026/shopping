<template>
  <router-link class="product" :to="'/user/goods/' + goods.id">
    <div class="picture">
      <img v-if="goods.cover && failed !== goods.cover" loading="lazy" :src="mediaUrl(goods.cover)" alt="" @error="failed = goods.cover" />
      <span v-else>暂无商品图</span>
    </div>
    <div class="caption"><strong>{{ goods.name }}</strong><span class="price">¥{{ Number(goods.price).toFixed(2) }}</span></div>
    <p v-if="goods.descr">{{ goods.descr }}</p>
    <small>已售 {{ goods.sales ?? 0 }}</small>
  </router-link>
</template>
<script setup>
import { ref } from 'vue'
import { mediaUrl } from '@/utils/format'
defineProps({ goods: { type: Object, required: true } })
const failed = ref(null)
</script>
<style scoped>
.product { display:block; min-width:0; color:#242722; text-decoration:none; }
.picture { display:grid; place-items:center; aspect-ratio: 1 / 1; overflow:hidden; background:#eae8e1; color:#64675e; font-size:13px; }
.picture img { width:100%; height:100%; min-height:0; object-fit:cover; transition:transform .35s ease; }
.caption { display:flex; align-items:baseline; justify-content:space-between; gap:6px 12px; flex-wrap:wrap; margin-top:14px; }
.caption strong { overflow-wrap:anywhere; font-size:16px; line-height:1.5; }
.price { font-variant-numeric:tabular-nums; font-weight:600; }
.product p { overflow:hidden; white-space:nowrap; text-overflow:ellipsis; margin:5px 0 0; font-size:13px; color:#5b6057; }
.product small { display:block; margin-top:6px; color:#63685d; }
@media(hover:hover) and (prefers-reduced-motion:no-preference) { .product:hover img { transform:scale(1.035); } }
.product:hover strong { text-decoration:underline; text-underline-offset:4px; }
</style>
