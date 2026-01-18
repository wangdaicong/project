<template>
  <div style="max-width: 920px; margin: 40px auto; padding: 0 16px">
    <el-card>
      <template #header>
        <div style="display: flex; align-items: center; justify-content: space-between">
          <div>Exan Web</div>
          <el-tag type="success">MVP</el-tag>
        </div>
      </template>

      <div style="display: flex; gap: 12px; flex-wrap: wrap; align-items: center">
        <el-text>学段：</el-text>
        <el-button
          v-for="s in stages"
          :key="s.id"
          :type="s.id === stageId ? 'primary' : 'default'"
          @click="selectStage(s.id)"
        >
          {{ s.name }}
        </el-button>
      </div>

      <div style="margin-top: 12px; display: flex; gap: 12px; flex-wrap: wrap; align-items: center">
        <el-text>年级：</el-text>
        <el-button
          v-for="g in gradeOptions"
          :key="g"
          :type="g === grade ? 'warning' : 'default'"
          @click="selectGrade(g)"
        >
          {{ g }}年级
        </el-button>
      </div>

      <div style="margin-top: 12px; display: flex; gap: 12px; flex-wrap: wrap; align-items: center">
        <el-text>学科：</el-text>
        <el-button
          v-for="s in subjects"
          :key="s.id"
          :type="s.id === subjectId ? 'success' : 'default'"
          @click="selectSubject(s.id)"
        >
          {{ s.name }}
        </el-button>
      </div>

      <el-divider />

      <div style="display: flex; align-items: center; justify-content: space-between">
        <el-text type="info">点击试卷可查看详情（内容解析待接入）</el-text>
        <el-button type="primary" :loading="loading" :disabled="!stageId || !subjectId" @click="loadPapers">刷新</el-button>
      </div>

      <div style="margin-top: 12px; display: flex; gap: 10px; flex-wrap: wrap; align-items: center">
        <el-text>省份：</el-text>
        <el-button :type="selectedProvince === '' ? 'primary' : 'default'" @click="selectProvince('')">全部</el-button>
        <el-button
          v-for="p in provinceOptions"
          :key="p"
          :type="selectedProvince === p ? 'primary' : 'default'"
          @click="selectProvince(p)"
        >
          {{ p }}
        </el-button>
      </div>

      <el-table
        :data="filteredPapers"
        v-loading="loading"
        style="width: 100%; margin-top: 12px"
        size="small"
        @row-click="onRowClick"
      >
        <el-table-column prop="paperDate" label="日期" width="140" />
        <el-table-column prop="title" label="试卷" />
      </el-table>

      <el-empty v-if="!loading && filteredPapers.length === 0" description="暂无试卷数据（等待同步/爬虫入库）" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listStages, listSubjects, type EduStage, type Subject } from '@/api/meta'
import { listPapers, type Paper } from '@/api/papers'

const loading = ref(false)

const stages = ref<EduStage[]>([])
const subjects = ref<Subject[]>([])

const allPapers = ref<Paper[]>([])

const stageId = ref<number>()
const subjectId = ref<number>()
const grade = ref<number>()
const selectedProvince = ref('')

const router = useRouter()

const gradeOptions = computed(() => {
  const s = stages.value.find((x) => x.id === stageId.value)
  const code = s?.code
  if (code === 'primary') return [1, 2, 3, 4, 5, 6]
  if (code === 'junior') return [7, 8, 9]
  if (code === 'senior') return [10, 11, 12]
  return []
})

const provinceOptions = computed(() => {
  const set = new Set<string>()
  allPapers.value.forEach((p) => {
    const prov = parseChinaProvince(p.title)
    if (prov) set.add(prov)
  })
  return Array.from(set).sort()
})

const filteredPapers = computed(() => {
  if (!selectedProvince.value) return allPapers.value
  return allPapers.value.filter((p) => parseChinaProvince(p.title) === selectedProvince.value)
})

async function loadMeta() {
  const resp = await listStages()
  stages.value = resp.data
  stageId.value = stages.value[0]?.id
  await refreshSubjects()
  refreshGrade()
  await loadPapers()
}

function refreshGrade() {
  const opts = gradeOptions.value
  if (!opts.length) {
    grade.value = undefined
    return
  }
  if (!grade.value || !opts.includes(grade.value)) {
    grade.value = opts[0]
  }
}

async function refreshSubjects() {
  if (!stageId.value) {
    subjects.value = []
    subjectId.value = undefined
    return
  }
  const resp = await listSubjects(stageId.value)
  subjects.value = resp.data
  subjectId.value = subjects.value[0]?.id
}

async function selectStage(id: number) {
  if (stageId.value === id) return
  stageId.value = id
  await refreshSubjects()
  refreshGrade()
  await loadPapers()
}

async function selectSubject(id: number) {
  subjectId.value = id
  await loadPapers()
}

async function selectGrade(g: number) {
  grade.value = g
  await loadPapers()
}

function selectProvince(p: string) {
  selectedProvince.value = p
}

async function loadPapers() {
  if (!stageId.value || !subjectId.value) return
  loading.value = true
  try {
    selectedProvince.value = ''
    const resp = await listPapers(stageId.value, subjectId.value, grade.value)
    allPapers.value = resp.data.items
  } finally {
    loading.value = false
  }
}

function parseChinaProvince(title: string): string {
  const t = (title ?? '').trim()
  if (!t) return ''

  const special = [
    '北京市',
    '天津市',
    '上海市',
    '重庆市',
    '内蒙古自治区',
    '广西壮族自治区',
    '西藏自治区',
    '宁夏回族自治区',
    '新疆维吾尔自治区',
    '香港特别行政区',
    '澳门特别行政区'
  ]
  for (const s of special) {
    if (t.includes(s)) return s.replace('市', '').replace('自治区', '').replace('特别行政区', '')
  }

  const provinces = [
    '河北省',
    '山西省',
    '辽宁省',
    '吉林省',
    '黑龙江省',
    '江苏省',
    '浙江省',
    '安徽省',
    '福建省',
    '江西省',
    '山东省',
    '河南省',
    '湖北省',
    '湖南省',
    '广东省',
    '海南省',
    '四川省',
    '贵州省',
    '云南省',
    '陕西省',
    '甘肃省',
    '青海省',
    '台湾省'
  ]
  for (const p of provinces) {
    if (t.includes(p)) return p.replace('省', '')
  }

  const m = t.match(/(北京|天津|上海|重庆|河北|山西|辽宁|吉林|黑龙江|江苏|浙江|安徽|福建|江西|山东|河南|湖北|湖南|广东|海南|四川|贵州|云南|陕西|甘肃|青海|台湾|内蒙古|广西|西藏|宁夏|新疆)\s*(省|市|自治区)?/)
  if (m && m[1]) return m[1]

  return ''
}

function onRowClick(row: Paper) {
  router.push({ path: `/papers/${row.id}` })
}

onMounted(loadMeta)
</script>
