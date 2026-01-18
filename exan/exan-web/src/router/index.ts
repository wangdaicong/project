import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '@/views/HomePage.vue'
import PracticePage from '@/views/PracticePage.vue'
import PapersPage from '../views/PapersPage.vue'
import PaperDetailPage from '../views/PaperDetailPage.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomePage
    },
    {
      path: '/practice',
      name: 'practice',
      component: PracticePage
    },
    {
      path: '/papers',
      name: 'papers',
      component: PapersPage
    },
    {
      path: '/papers/:id',
      name: 'paperDetail',
      component: PaperDetailPage
    }
  ]
})

export default router
