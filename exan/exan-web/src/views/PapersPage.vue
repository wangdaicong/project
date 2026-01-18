<template>
  <div style="max-width: 1100px; margin: 24px auto; padding: 0 16px">
    <el-card>
      <template #header>
        <div style="display: flex; align-items: center; justify-content: space-between">
          <div>试卷</div>
          <div style="display: flex; gap: 8px">
            <el-button @click="$router.push('/')">返回首页</el-button>
          </div>
        </div>
      </template>

      <div style="display: flex; gap: 12px; flex-wrap: wrap; align-items: center">
        <el-select v-model="stageId" placeholder="选择学段" style="width: 160px" @change="onStageChange">
          <el-option v-for="s in stages" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>

        <el-select v-model="subjectId" placeholder="选择学科" style="width: 160px" @change="loadPapers">
          <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>

        <el-button type="primary" :loading="loading" @click="loadPapers">刷新</el-button>
      </div>

      <el-divider />

      <el-table :data="papers" v-loading="loading" style="width: 100%" size="small">
        <el-table-column prop="paperDate" label="日期" width="140" />
        <el-table-column prop="title" label="试卷" />
        <el-table-column prop="regionCode" label="地区" width="120" />
      </el-table>

      <el-empty v-if="!loading && papers.length === 0" description="暂无试卷数据（等待爬虫同步）" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { listStages, listSubjects, type EduStage, type Subject } from '@/api/meta'
import { listPapers, type Paper } from '@/api/papers'

const route = useRoute()
const router = useRouter()

const stages = ref<EduStage[]>([])
const subjects = ref<Subject[]>([])

const stageId = ref<number>()
const subjectId = ref<number>()

const loading = ref(false)
const papers = ref<Paper[]>([])

async function loadMeta() {
  const s = await listStages()
  stages.value = s.data

  const qsStage = Number(route.query.stageId)
  const qsSubject = Number(route.query.subjectId)

  if (Number.isFinite(qsStage) && qsStage > 0) {
    stageId.value = qsStage
  } else {
    stageId.value = stages.value[0]?.id
  }

  await onStageChange()

  if (Number.isFinite(qsSubject) && qsSubject > 0) {
    subjectId.value = qsSubject
  }

  await loadPapers()
}

async function onStageChange() {
  papers.value = []
  if (!stageId.value) return
  const resp = await listSubjects(stageId.value)
  subjects.value = resp.data
  if (!subjects.value.find((s) => s.id === subjectId.value)) {
    subjectId.value = subjects.value[0]?.id
  }
}

async function loadPapers() {
  if (!stageId.value || !subjectId.value) return
  loading.value = true
  try {
    router.replace({
      path: '/papers',
      query: { stageId: String(stageId.value), subjectId: String(subjectId.value) }
    })
    const resp = await listPapers(stageId.value, subjectId.value)
    papers.value = resp.data.items
  } catch (e: any) {
    papers.value = []
    ElMessage.error(e?.response?.data?.message ?? e?.message ?? '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadMeta)
</script>
