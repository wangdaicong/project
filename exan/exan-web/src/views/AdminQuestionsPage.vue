<template>
  <div style="max-width: 1100px; margin: 24px auto; padding: 0 16px">
    <el-card>
      <template #header>
        <div style="display: flex; align-items: center; justify-content: space-between">
          <div>后台：题目导入/审核</div>
          <el-button @click="$router.push('/')">返回首页</el-button>
        </div>
      </template>

      <div style="display: flex; gap: 12px; flex-wrap: wrap; align-items: center">
        <el-select v-model="stageId" placeholder="选择学段" style="width: 160px" @change="onStageChange">
          <el-option v-for="s in stages" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>

        <el-select v-model="subjectId" placeholder="选择学科" style="width: 160px">
          <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>

        <el-button type="primary" :loading="loading" @click="loadJobs">刷新任务列表</el-button>
      </div>

      <el-divider />

      <el-alert
        title="导入格式：粘贴 JSON 数组，每项包含 stageId/subjectId/type/stem/answer/options"
        type="info"
        :closable="false"
      />

      <div style="margin-top: 12px">
        <el-input
          v-model="jsonText"
          type="textarea"
          :rows="10"
          placeholder='例如：[{"stageId":1,"subjectId":1,"type":"SINGLE","stem":"1+1=?","answer":"A","options":[{"key":"A","content":"2"},{"key":"B","content":"3"}]}]'
        />
      </div>

      <div style="margin-top: 12px; display: flex; gap: 12px; flex-wrap: wrap; align-items: center">
        <el-button type="success" :loading="importing" @click="doImport">创建导入任务（JSON粘贴）</el-button>
        <el-upload :auto-upload="false" :show-file-list="false" accept=".json" @change="onFileChange">
          <el-button type="success" plain :loading="uploading">上传JSON文件并导入</el-button>
        </el-upload>
        <el-button @click="fillSample">填充示例</el-button>
      </div>

      <el-divider />

      <div style="display: flex; gap: 12px; flex-wrap: wrap; align-items: center">
        <el-select v-model="selectedJobId" placeholder="选择导入任务" style="width: 260px" @change="onSelectJob">
          <el-option
            v-for="j in jobs"
            :key="j.id"
            :label="`#${j.id} 新增${j.insertedCount}/重复${j.duplicateCount}/失败${j.failedCount}`"
            :value="j.id"
          />
        </el-select>

        <el-button type="primary" :disabled="!selectedJobId" @click="refreshPendingByJob">查看本任务待审核题</el-button>
        <el-button type="success" :disabled="!selectedJobId" @click="approveAll">本任务一键通过</el-button>
        <el-button type="danger" :disabled="!selectedJobId" @click="rejectAll">本任务一键驳回</el-button>
      </div>

      <el-table :data="pending" v-loading="loading" style="width: 100%" size="small">
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="type" label="题型" width="90" />
        <el-table-column prop="status" label="状态" width="110" />
        <el-table-column prop="difficulty" label="难度" width="80" />
        <el-table-column label="题干">
          <template #default="scope">
            <div style="max-height: 48px; overflow: hidden">
              <span v-html="scope.row.stem" />
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" type="primary" @click="approve(scope.row.id)">通过</el-button>
            <el-button size="small" type="danger" @click="reject(scope.row.id)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listStages, listSubjects, type EduStage, type Subject } from '@/api/meta'
import {
  approveQuestion,
  rejectQuestion,
  type ImportQuestionItem,
  type Question
} from '@/api/adminQuestions'
import {
  approveAllByJob,
  createJobFromJson,
  createJobFromJsonFile,
  listJobs,
  listPendingQuestionsByJob,
  type ImportJob
} from '@/api/importJobs'

const stages = ref<EduStage[]>([])
const subjects = ref<Subject[]>([])

const stageId = ref<number>()
const subjectId = ref<number>()

const jsonText = ref('')
const importing = ref(false)
const uploading = ref(false)
const loading = ref(false)

const pending = ref<Question[]>([])

const jobs = ref<ImportJob[]>([])
const selectedJobId = ref<number>()

async function loadMeta() {
  const s = await listStages()
  stages.value = s.data
  if (stages.value.length) {
    stageId.value = stages.value[0].id
    await onStageChange()
  }
}

async function onStageChange() {
  if (!stageId.value) return
  const resp = await listSubjects(stageId.value)
  subjects.value = resp.data
  subjectId.value = subjects.value[0]?.id
  await loadJobs()
}

function fillSample() {
  if (!stageId.value || !subjectId.value) {
    ElMessage.warning('请先选择学段/学科')
    return
  }
  const sample: ImportQuestionItem[] = [
    {
      stageId: stageId.value,
      subjectId: subjectId.value,
      type: 'SINGLE',
      stem: '1 + 1 = ?',
      difficulty: 1,
      answer: 'A',
      analysis: '1+1=2',
      options: [
        { key: 'A', content: '2' },
        { key: 'B', content: '3' },
        { key: 'C', content: '4' },
        { key: 'D', content: '5' }
      ]
    }
  ]
  jsonText.value = JSON.stringify(sample, null, 2)
}

async function doImport() {
  if (!jsonText.value.trim()) {
    ElMessage.warning('请粘贴JSON')
    return
  }
  importing.value = true
  try {
    const arr = JSON.parse(jsonText.value)
    if (!Array.isArray(arr)) {
      ElMessage.error('必须是JSON数组')
      return
    }
    const resp = await createJobFromJson(arr)
    ElMessage.success(`已创建任务 #${resp.data.jobId}：新增${resp.data.insertedCount}/重复${resp.data.duplicateCount}/失败${resp.data.failedCount}`)
    await loadJobs()
    selectedJobId.value = resp.data.jobId
    await refreshPendingByJob()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message ?? e?.message ?? '导入失败')
  } finally {
    importing.value = false
  }
}

async function onFileChange(uploadFile: any) {
  const file: File | undefined = uploadFile?.raw
  if (!file) return
  uploading.value = true
  try {
    const resp = await createJobFromJsonFile(file)
    ElMessage.success(`已创建任务 #${resp.data.jobId}：新增${resp.data.insertedCount}/重复${resp.data.duplicateCount}/失败${resp.data.failedCount}`)
    await loadJobs()
    selectedJobId.value = resp.data.jobId
    await refreshPendingByJob()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message ?? e?.message ?? '上传导入失败')
  } finally {
    uploading.value = false
  }
}

async function loadJobs() {
  loading.value = true
  try {
    const resp = await listJobs(20)
    jobs.value = resp.data
    if (!selectedJobId.value && jobs.value.length) {
      selectedJobId.value = jobs.value[0].id
    }
  } finally {
    loading.value = false
  }
}

async function onSelectJob() {
  await refreshPendingByJob()
}

async function refreshPendingByJob() {
  if (!selectedJobId.value) return
  loading.value = true
  try {
    const resp = await listPendingQuestionsByJob(selectedJobId.value, 200)
    pending.value = resp.data
  } finally {
    loading.value = false
  }
}

async function approveAll() {
  if (!selectedJobId.value) return
  const resp = await approveAllByJob(selectedJobId.value)
  ElMessage.success(`本任务已通过 ${resp.data} 道题`)
  await refreshPendingByJob()
}

async function rejectAll() {
  if (!selectedJobId.value) return
  const resp = await rejectAllByJob(selectedJobId.value)
  ElMessage.success(`本任务已驳回 ${resp.data} 道题`)
  await refreshPendingByJob()
}

async function approve(id: number) {
  await approveQuestion(id)
  ElMessage.success('已通过')
  await refreshPendingByJob()
}

async function reject(id: number) {
  await rejectQuestion(id)
  ElMessage.success('已驳回')
  await refreshPendingByJob()
}

onMounted(loadMeta)
</script>
