<template>
  <div style="max-width: 980px; margin: 24px auto; padding: 0 16px">
    <el-card>
      <template #header>
        <div style="display: flex; align-items: center; justify-content: space-between">
          <div>练习</div>
          <div style="display: flex; gap: 8px">
            <el-button @click="$router.push('/')">返回首页</el-button>
          </div>
        </div>
      </template>

      <div style="display: flex; gap: 12px; flex-wrap: wrap; align-items: center">
        <el-select v-model="stageId" placeholder="选择学段" style="width: 160px" @change="onStageChange">
          <el-option v-for="s in stages" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>

        <el-select v-model="subjectId" placeholder="选择学科" style="width: 160px">
          <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>

        <el-input-number v-model="count" :min="1" :max="20" />

        <el-button type="primary" :loading="creating" @click="create">开始练习</el-button>

        <el-tag v-if="session" type="info">会话：{{ session.sessionId }} / {{ session.status }}</el-tag>
        <el-tag v-if="session?.scoreTotal" type="success">得分：{{ session.scoreGot ?? 0 }}/{{ session.scoreTotal }}</el-tag>
      </div>

      <el-divider />

      <div v-if="session">
        <el-space direction="vertical" style="width: 100%" :size="12">
          <el-card v-for="(q, idx) in session.questions" :key="q.id">
            <div style="display: flex; justify-content: space-between; gap: 12px">
              <div style="font-weight: 600">{{ idx + 1 }}. <span v-html="q.stem" /></div>
              <el-tag type="warning">{{ q.type }}</el-tag>
            </div>

            <div style="margin-top: 10px">
              <template v-if="q.type === 'SINGLE'">
                <el-radio-group v-model="answers[q.id]" @change="(val) => onAnswer(q.id, val)">
                  <el-space direction="vertical" alignment="start">
                    <el-radio v-for="op in q.options" :key="op.key" :label="op.key">
                      {{ op.key }}. <span v-html="op.content" />
                    </el-radio>
                  </el-space>
                </el-radio-group>
              </template>

              <template v-else>
                <el-input
                  v-model="answers[q.id]"
                  placeholder="输入你的答案（MVP仅做客观题严格匹配）"
                  @change="(val) => onAnswer(q.id, val)"
                />
              </template>
            </div>
          </el-card>
        </el-space>

        <div style="margin-top: 16px; display: flex; gap: 12px">
          <el-button type="success" :loading="submitting" @click="submit">交卷</el-button>
          <el-button @click="refresh">刷新会话</el-button>
        </div>

        <el-divider />

        <div>
          <el-button type="info" :loading="lbLoading" @click="loadLeaderboard">查看日榜</el-button>
          <el-table v-if="leaderboard.length" :data="leaderboard" style="margin-top: 12px" size="small">
            <el-table-column prop="rank" label="排名" width="80" />
            <el-table-column prop="userId" label="用户ID" width="120" />
            <el-table-column prop="score" label="分数" />
          </el-table>
        </div>
      </div>

      <el-empty v-else description="请先选择学段/学科并开始练习" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listStages, listSubjects, type EduStage, type Subject } from '@/api/meta'
import {
  createPracticeSession,
  getPracticeSessionDetail,
  submitAnswer,
  submitSession,
  type SessionDetailResponse
} from '@/api/practice'
import { getDailyLeaderboard, type LeaderboardItem } from '@/api/leaderboard'

const stages = ref<EduStage[]>([])
const subjects = ref<Subject[]>([])

const stageId = ref<number>()
const subjectId = ref<number>()
const count = ref(10)

const creating = ref(false)
const submitting = ref(false)

const session = ref<SessionDetailResponse | null>(null)
const answers = ref<Record<number, any>>({})

const lbLoading = ref(false)
const leaderboard = ref<LeaderboardItem[]>([])

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
}

async function create() {
  if (!stageId.value || !subjectId.value) {
    ElMessage.warning('请选择学段/学科')
    return
  }
  creating.value = true
  try {
    const resp = await createPracticeSession({ stageId: stageId.value, subjectId: subjectId.value, count: count.value })
    const id = resp.data
    await loadSession(id)
    ElMessage.success('已创建练习会话')
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message ?? e?.message ?? '创建失败')
  } finally {
    creating.value = false
  }
}

async function loadSession(id: number) {
  const resp = await getPracticeSessionDetail(id)
  session.value = resp.data
  // 初始化答案容器
  const m: Record<number, any> = { ...answers.value }
  session.value.questions.forEach((q) => {
    if (!(q.id in m)) m[q.id] = ''
  })
  answers.value = m
}

async function refresh() {
  if (!session.value) return
  await loadSession(session.value.sessionId)
}

async function onAnswer(questionId: number, val: any) {
  if (!session.value) return
  try {
    await submitAnswer(session.value.sessionId, { questionId, answer: val })
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message ?? e?.message ?? '提交答案失败')
  }
}

async function submit() {
  if (!session.value) return
  submitting.value = true
  try {
    const resp = await submitSession(session.value.sessionId)
    session.value = resp.data
    ElMessage.success(`已交卷，得分 ${session.value.scoreGot ?? 0}/${session.value.scoreTotal ?? 0}`)
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message ?? e?.message ?? '交卷失败')
  } finally {
    submitting.value = false
  }
}

async function loadLeaderboard() {
  if (!session.value?.subjectId) return
  lbLoading.value = true
  try {
    const resp = await getDailyLeaderboard(session.value.subjectId, 20)
    leaderboard.value = resp.data
  } finally {
    lbLoading.value = false
  }
}

onMounted(loadMeta)
</script>
