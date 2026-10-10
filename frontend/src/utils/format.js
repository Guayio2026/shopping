export function resolveImageUrl(url) {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/uploads/')) return `/api${url}`
  return url
}

/** Resolve media URL (cover / video / avatar) */
export function mediaUrl(url) {
  if (!url) return ''
  if (/^https?:\/\//i.test(url) || url.startsWith('data:')) return url
  if (url.startsWith('/uploads/')) return `/api${url}`
  if (url.startsWith('/')) return url
  return `/uploads/${url.replace(/^\/+/, '')}`
}

export function formatDuration(seconds) {
  const s = Number(seconds) || 0
  const m = Math.floor(s / 60)
  const r = s % 60
  return `${m}:${String(r).padStart(2, '0')}`
}

export function formatPrice(price) {
  const n = Number(price)
  if (!n || n <= 0) return '免费'
  return `¥${n.toFixed(2)}`
}

export function levelLabel(level) {
  const map = {
    BEGINNER: '入门',
    INTERMEDIATE: '进阶',
    ADVANCED: '高级',
    入门: '入门',
    进阶: '进阶',
    高级: '高级',
    实战: '实战'
  }
  return map[level] || level || '通用'
}

export function parseOptions(options) {
  if (!options) return []
  if (Array.isArray(options)) return options
  try {
    const parsed = JSON.parse(options)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return String(options)
      .split(/[,|]/)
      .map((s) => s.trim())
      .filter(Boolean)
  }
}

export function questionTypeLabel(type) {
  const map = {
    SINGLE: '单选题',
    MULTIPLE: '多选题',
    BLANK: '填空题',
    ESSAY: '简答题',
    CODE: '编程题'
  }
  return map[type] || type || '题目'
}

/** Strip HTML tags for list excerpts / validation */
export function plainTextOf(html) {
  return String(html || '')
    .replace(/<[^>]+>/g, ' ')
    .replace(/&nbsp;/gi, ' ')
    .replace(/&lt;/gi, '<')
    .replace(/&gt;/gi, '>')
    .replace(/&amp;/gi, '&')
    .replace(/\s+/g, ' ')
    .trim()
}

/** Render post/article body: keep HTML, wrap plain text as paragraphs */
export function toContentHtml(raw, emptyText = '暂无内容') {
  const source = String(raw || '').trim()
  const plain = plainTextOf(source)
  if (!plain) return `<p>${emptyText}</p>`
  if (/<[a-z][\s\S]*>/i.test(source)) return source
  return source
    .split(/\n+/)
    .map((line) => line.trim())
    .filter(Boolean)
    .map((p) => `<p>${p}</p>`)
    .join('')
}
