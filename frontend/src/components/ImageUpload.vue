<template>
  <div
    class="image-upload"
    :class="[`is-${type}`, { 'has-value': !!modelValue, 'is-placeholder': showingPlaceholder }]"
  >
    <el-upload
      :show-file-list="false"
      :http-request="handleUpload"
      accept="image/jpeg,image/png,image/gif,image/webp,image/svg+xml,.svg,image/*"
      :disabled="uploading"
    >
      <!-- 头像 / Logo：圆形；SVG 用 img 预览，避免 el-avatar 不渲染 -->
      <div v-if="type === 'avatar'" class="avatar-box" :style="avatarBoxStyle">
        <img v-if="previewUrl" :src="previewUrl" class="avatar-img-el" alt="logo" />
        <el-avatar v-else :size="avatarSize" class="avatar-img">
          {{ fallbackText }}
        </el-avatar>
        <span class="mask">
          <span>{{ uploading ? '上传中…' : uploadText || '上传头像' }}</span>
        </span>
      </div>

      <!-- 封面：矩形 -->
      <template v-else>
        <div v-if="modelValue" class="cover-preview" :style="coverBoxStyle">
          <img :src="previewUrl" alt="cover" />
          <span class="mask">{{ uploading ? '上传中…' : '更换' }}</span>
        </div>
        <el-button v-else type="primary" plain :loading="uploading">
          {{ uploadText || '上传图片' }}
        </el-button>
      </template>
    </el-upload>

    <el-button
      v-if="clearable && modelValue && type !== 'avatar'"
      link
      type="danger"
      class="clear-btn"
      @click.stop="clear"
    >
      清除
    </el-button>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadFile } from '@/api/upload'
import { mediaUrl } from '@/utils/format'

const props = defineProps({
  modelValue: { type: String, default: '' },
  /** cover | avatar */
  type: { type: String, default: 'cover' },
  uploadText: { type: String, default: '' },
  clearable: { type: Boolean, default: true },
  tip: { type: String, default: '' },
  width: { type: [Number, String], default: 200 },
  height: { type: [Number, String], default: 112 },
  avatarSize: { type: Number, default: 96 },
  fallback: { type: String, default: '图' },
  /** 无自定义值时展示的占位图（如默认 Logo） */
  placeholderSrc: { type: String, default: '' },
  showSuccess: { type: Boolean, default: true }
})

const emit = defineEmits(['update:modelValue', 'success'])

const uploading = ref(false)

const previewUrl = computed(() => {
  const custom = mediaUrl(props.modelValue)
  if (custom) return custom
  return props.placeholderSrc || ''
})
const fallbackText = computed(() => (props.fallback || '图').slice(0, 1).toUpperCase())
const showingPlaceholder = computed(() => !props.modelValue && !!props.placeholderSrc)

const coverBoxStyle = computed(() => ({
  width: typeof props.width === 'number' ? `${props.width}px` : props.width,
  height: typeof props.height === 'number' ? `${props.height}px` : props.height
}))

const avatarBoxStyle = computed(() => ({
  width: `${props.avatarSize}px`,
  height: `${props.avatarSize}px`
}))

async function handleUpload({ file }) {
  if (!file) return
  const name = file.name || ''
  const type = String(file.type || '').toLowerCase()
  const isImage =
    type.startsWith('image/') ||
    type === 'image/svg+xml' ||
    /\.(jpe?g|png|gif|webp|svg)$/i.test(name)
  if (!isImage) {
    ElMessage.warning('请上传图片文件（jpg / png / gif / webp / svg）')
    return
  }
  uploading.value = true
  try {
    const data = await uploadFile(file)
    const url = data?.url || data?.path || data || ''
    emit('update:modelValue', url)
    emit('success', url)
    if (props.showSuccess) {
      ElMessage.success('上传成功')
    }
  } finally {
    uploading.value = false
  }
}

function clear() {
  emit('update:modelValue', '')
}
</script>

<style scoped>
.image-upload {
  display: inline-flex;
  align-items: flex-end;
  flex-wrap: wrap;
  gap: 8px;
}

.image-upload :deep(.el-upload) {
  display: inline-block;
}

.cover-preview {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
  cursor: pointer;
  background: #f5f7fa;
}

.cover-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.avatar-box {
  position: relative;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  flex-shrink: 0;
}

.avatar-img {
  display: block;
}

.avatar-img-el {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
  background: #f5f7fa;
}

.mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(15, 23, 42, 0.45);
  color: #fff;
  font-size: 13px;
  opacity: 0;
  transition: opacity 0.2s;
  text-align: center;
  padding: 4px;
  line-height: 1.3;
}

.cover-preview:hover .mask,
.avatar-box:hover .mask {
  opacity: 1;
}

.clear-btn {
  margin-bottom: 2px;
}
</style>
