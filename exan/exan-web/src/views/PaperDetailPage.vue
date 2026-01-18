<template>
  <div style="max-width: 1100px; margin: 24px auto; padding: 0 16px">
    <el-card>
      <template #header>
        <div style="display: flex; align-items: center; justify-content: space-between">
          <div>试卷详情</div>
          <div style="display: flex; gap: 8px">
            <el-button @click="$router.push('/')">返回首页</el-button>
          </div>
        </div>
      </template>

      <div v-loading="loading">
        <div v-if="paper">
          <div style="display: flex; align-items: center; justify-content: space-between; gap: 10px">
            <div style="display: flex; align-items: center; gap: 10px; font-weight: 600; font-size: 16px; min-width: 0">
              <span style="overflow: hidden; text-overflow: ellipsis; white-space: nowrap">{{ paper.title }}</span>
            </div>
            <div style="display: flex; gap: 8px">
              <el-button type="primary" plain @click="addToLesson">加入备课</el-button>
            </div>
          </div>
          <div style="margin-top: 8px; color: #666; font-size: 12px">
            <span>日期：{{ paper.paperDate }}</span>
            <span v-if="paper.views != null" style="margin-left: 12px">浏览：{{ paper.views }}</span>
            <span v-if="paper.downloads != null" style="margin-left: 12px">下载：{{ paper.downloads }}</span>
            <span v-if="meta.owner" style="margin-left: 12px">所属：{{ meta.owner }}</span>
          </div>
        </div>

        <el-empty v-else-if="!loading" description="未找到试卷" />

        <el-divider />

        <div v-if="paper">
          <el-alert
            v-if="!images.length && !pdfs.length"
            title="暂无预览内容"
            type="info"
            :closable="false"
            description="尚未抓取试卷预览图/PDF，请先通过后台同步接口抓取。"
          />

          <div
            v-else
            ref="panelRef"
            :style="{ display: 'flex', gap: '16px', alignItems: 'stretch', height: panelHeight + 'px' }"
          >
            <div style="flex: 1; min-width: 0; display: flex; flex-direction: column; min-height: 0">
              <div
                v-if="images.length"
                style="display: flex; align-items: center; justify-content: space-between; gap: 12px; margin-bottom: 10px; flex: none"
              >
                <div style="display: flex; align-items: center; justify-content: center; gap: 12px">
                  <el-button :disabled="currentIndex <= 0" @click="prevPage">上一页</el-button>
                  <div style="color: #666; font-size: 12px">{{ currentIndex + 1 }} / {{ totalPages }}</div>
                  <el-button :disabled="currentIndex >= images.length - 1" @click="nextPage">下一页</el-button>
                </div>
                <el-button size="small" @click="leftOpen = !leftOpen">{{ leftOpen ? '隐藏页码' : '显示页码' }}</el-button>
              </div>

              <div
                v-if="images.length"
                ref="viewerRef"
                @scroll="onViewerScroll"
                :style="{
                  border: '1px solid #eee',
                  borderRadius: '6px',
                  padding: '8px',
                  background: '#fafafa',
                  flex: 1,
                  minHeight: 0,
                  overflow: 'auto'
                }"
              >
                <div style="display: flex; flex-direction: column; gap: 12px">
                  <div
                    v-for="(img, idx) in images"
                    :key="img.url + '_' + idx"
                    :ref="(el) => setPageEl(el, idx)"
                    :data-idx="idx"
                    :style="{
                      border: idx === currentIndex ? '2px solid #409EFF' : '1px solid #e6e6e6',
                      borderRadius: '6px',
                      padding: '10px',
                      background: '#fff'
                    }"
                  >
                    <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px">
                      <div style="font-size: 12px; color: #666; font-weight: 600">第 {{ img.page ?? (idx + 1) }} 页</div>
                      <div style="font-size: 12px; color: #999">{{ idx + 1 }} / {{ totalPages }}</div>
                    </div>
                    <el-image
                      :src="img.url"
                      style="width: 100%; height: auto; display: block"
                      :preview-src-list="images.map((x) => x.url)"
                      :initial-index="idx"
                      preview-teleported
                    />
                  </div>
                </div>
              </div>

              <div v-if="pdfs.length" style="margin-top: 12px">
                <div style="font-weight: 600; margin-bottom: 6px">PDF</div>
                <el-link
                  v-for="(p, idx) in pdfs"
                  :key="idx"
                  :href="p.url"
                  target="_blank"
                  type="primary"
                  style="display: block"
                  @click.prevent="onClickPdf(p)"
                >
                  {{ p.label || '下载 PDF' }}
                </el-link>
              </div>
            </div>

            <div
              v-if="images.length && leftOpen"
              :style="{
                width: '180px',
                borderLeft: '1px solid #eee',
                paddingLeft: '12px',
                display: 'flex',
                flexDirection: 'column',
                height: '100%',
                minHeight: 0
              }"
            >
              <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; flex: none">
                <div style="font-weight: 600; font-size: 12px; color: #666">页码</div>
                <div style="color: #666; font-size: 12px">{{ currentIndex + 1 }} / {{ totalPages }}</div>
              </div>
              <el-scrollbar :height="Math.max(panelHeight - 40, 200) + 'px'">
                <div
                  v-for="(img, idx) in images"
                  :key="idx"
                  :ref="(el) => setThumbEl(el, idx)"
                  @click="scrollToIndex(idx)"
                  :style="{
                    border: idx === currentIndex ? '2px solid #409EFF' : '1px solid #eee',
                    borderRadius: '6px',
                    padding: '6px',
                    marginBottom: '10px',
                    cursor: 'pointer',
                    background: '#fff'
                  }"
                >
                  <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 6px">
                    <div style="font-size: 12px; color: #666">第 {{ img.page ?? (idx + 1) }} 页</div>
                  </div>
                  <el-image :src="img.url" style="width: 100%; height: 180px" fit="contain" />
                </div>
              </el-scrollbar>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPaperDetail, incPaperDownload } from '@/api/papers'

const route = useRoute()

const loading = ref(false)
type Attachment = { type?: string; url: string; label?: string; page?: number }
type Meta = { owner?: string | null; totalPages?: number | null }

type PaperDetail = {
  id: number
  stageId: number
  subjectId: number
  title: string
  paperDate: string
  regionCode?: string
  sourceUrl?: string
  views?: number
  downloads?: number
  contentText?: string
  attachmentsJson?: string
}

const paper = ref<PaperDetail | null>(null)
const attachments = ref<Attachment[]>([])
const images = ref<Attachment[]>([])
const pdfs = ref<Attachment[]>([])
const currentIndex = ref(0)
const meta = ref<Meta>({})
const totalPages = ref(0)
const leftOpen = ref(true)
const panelRef = ref<HTMLElement | null>(null)
const panelHeight = ref(600)
const viewerRef = ref<HTMLElement | null>(null)
const pageEls = ref<HTMLElement[]>([])
const thumbEls = ref<HTMLElement[]>([])
let scrollRaf: number | null = null

function recalcPanelHeight() {
  const top = panelRef.value?.getBoundingClientRect?.().top
  const topPx = typeof top === 'number' && Number.isFinite(top) ? top : 260
  const v = Math.max(window.innerHeight - topPx - 24, 520)
  panelHeight.value = v
}

function setPageEl(el: any, idx: number) {
  if (!el) return
  pageEls.value[idx] = el as HTMLElement
}

function setThumbEl(el: any, idx: number) {
  if (!el) return
  thumbEls.value[idx] = el as HTMLElement
}

function scrollThumbIntoView(idx: number) {
  const el = thumbEls.value[idx]
  if (!el) return
  try {
    el.scrollIntoView({ block: 'nearest' })
  } catch {
  }
}

function scrollToIndex(idx: number, behavior: ScrollBehavior = 'smooth') {
  const i = Math.max(0, Math.min(idx, images.value.length - 1))
  currentIndex.value = i
  nextTick(() => {
    const container = viewerRef.value
    const el = pageEls.value[i]
    if (!container || !el) return
    try {
      const cRect = container.getBoundingClientRect()
      const eRect = el.getBoundingClientRect()
      const top = eRect.top - cRect.top + container.scrollTop
      container.scrollTo({ top: Math.max(0, top - 8), behavior })
    } catch {
      try {
        el.scrollIntoView({ block: 'start', behavior })
      } catch {
      }
    }
    scrollThumbIntoView(i)
  })
}

function addToLesson() {
  ElMessage.success('已加入备课（示意）')
}

function prevPage() {
  if (currentIndex.value > 0) scrollToIndex(currentIndex.value - 1)
}

function nextPage() {
  if (currentIndex.value < images.value.length - 1) scrollToIndex(currentIndex.value + 1)
}

async function onClickPdf(p: Attachment) {
  try {
    if (!paper.value?.id) {
      window.open(p.url, '_blank')
      return
    }
    const resp = await incPaperDownload(paper.value.id)
    if (paper.value) {
      ;(paper.value as any).downloads = resp.data as any
    }
  } catch {
  } finally {
    window.open(p.url, '_blank')
  }
}

function parseAttachmentsJson(raw?: string): { items: Attachment[]; meta: Meta } {
  if (!raw) return { items: [], meta: {} }
  try {
    const parsed = JSON.parse(raw)
    if (Array.isArray(parsed)) {
      return { items: parsed.filter((x) => x && typeof x.url === 'string'), meta: {} }
    }
    if (parsed && typeof parsed === 'object') {
      const items = Array.isArray((parsed as any).items) ? (parsed as any).items : []
      const m = (parsed as any).meta && typeof (parsed as any).meta === 'object' ? (parsed as any).meta : {}
      return { items: items.filter((x: any) => x && typeof x.url === 'string'), meta: m }
    }
    return { items: [], meta: {} }
  } catch {
    return { items: [], meta: {} }
  }
}

async function load() {
  const id = Number(route.params.id)
  if (!Number.isFinite(id) || id <= 0) return

  loading.value = true
  try {
    const resp = await getPaperDetail(id)
    paper.value = resp.data as any
    attachments.value = []
    images.value = []
    pdfs.value = []
    currentIndex.value = 0
    meta.value = {}
    totalPages.value = 0
    leftOpen.value = true

    const parsed = parseAttachmentsJson(paper.value?.attachmentsJson)
    attachments.value = parsed.items
    meta.value = parsed.meta ?? {}

    images.value = attachments.value
      .filter((x) => (x.type ?? '') === 'image' && typeof x.url === 'string')
      .slice()
      .sort((a, b) => Number(a.page ?? 999999) - Number(b.page ?? 999999))
    pdfs.value = attachments.value.filter((x) => (x.type ?? '') === 'pdf' && typeof x.url === 'string')

    totalPages.value = images.value.length
  } catch (e: any) {
    paper.value = null
    attachments.value = []
    images.value = []
    pdfs.value = []
    meta.value = {}
    totalPages.value = 0
    ElMessage.error(e?.response?.data?.message ?? e?.message ?? '加载失败')
  } finally {
    loading.value = false
  }
}

function updateIndexByScroll() {
  const container = viewerRef.value
  if (!container) return
  const cRect = container.getBoundingClientRect()
  const anchor = cRect.top + 40
  let bestIdx = currentIndex.value
  let bestDist = Number.POSITIVE_INFINITY
  for (let i = 0; i < pageEls.value.length; i++) {
    const el = pageEls.value[i]
    if (!el) continue
    const r = el.getBoundingClientRect()
    const dist = Math.abs(r.top - anchor)
    if (dist < bestDist) {
      bestDist = dist
      bestIdx = i
    }
  }
  if (Number.isFinite(bestIdx) && bestIdx !== currentIndex.value) {
    currentIndex.value = bestIdx
    nextTick(() => scrollThumbIntoView(bestIdx))
  }
}

function onViewerScroll() {
  if (scrollRaf != null) return
  scrollRaf = requestAnimationFrame(() => {
    scrollRaf = null
    updateIndexByScroll()
  })
}

watch(
  () => images.value.length,
  async () => {
    pageEls.value = []
    thumbEls.value = []
    await nextTick()
    nextTick(() => scrollToIndex(currentIndex.value, 'auto'))
  }
)

onMounted(() => {
  nextTick(() => recalcPanelHeight())
  window.addEventListener('resize', recalcPanelHeight)
  load()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', recalcPanelHeight)
  if (scrollRaf != null) {
    try {
      cancelAnimationFrame(scrollRaf)
    } catch {
    }
    scrollRaf = null
  }
})
</script>
