import axios from 'axios'
import { ElMessage } from 'element-plus'

export async function uploadFile(file, options = {}) {
  const formData = new FormData()
  formData.append('file', file)

  const token = localStorage.getItem('token')
  const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'
  const isVideo = typeof file?.type === 'string' && file.type.startsWith('video/')
    || /\.(mp4|webm|mov|avi|mkv|m4v)$/i.test(file?.name || '')

  try {
    const response = await axios.post(`${baseURL}/files/upload`, formData, {
      headers: {
        Authorization: token ? `Bearer ${token}` : undefined
        // 不要手动设置 Content-Type，让浏览器自动带 boundary
      },
      timeout: options.timeout || (isVideo ? 10 * 60 * 1000 : 60000),
      onUploadProgress: options.onProgress
    })

    const result = response.data
    if (result.code !== 200) {
      ElMessage.error(result.message || '上传失败')
      throw new Error(result.message || '上传失败')
    }

    return result.data
  } catch (error) {
    if (error?.response) {
      const status = error.response.status
      const msg =
        error.response.data?.message ||
        (status === 413 ? '文件过大，请压缩后重试' : null) ||
        error.message ||
        '上传失败'
      ElMessage.error(msg)
      throw new Error(msg)
    }
    if (error?.message && !String(error.message).includes('上传')) {
      ElMessage.error(error.message || '上传失败')
    }
    throw error
  }
}
