const app = getApp()

Page({
  data: {
    degreeLevel: '本科',
    selectedCategory: '',
    selectedSubCategory: '',
    
    categories: ['工学', '医学', '文学', '管理学', '理学', '经济学', '法学', '教育学', '艺术学', '农学', '历史学', '哲学'],
    subCategories: [],
    
    majors: [],
    page: 1,
    size: 20,
    loading: false,
    hasMore: true
  },

  onLoad(options) {
    this.loadCategories()
    this.loadMajors()
  },

  switchDegree(e) {
    const level = e.currentTarget.dataset.level
    this.setData({
      degreeLevel: level,
      page: 1,
      majors: [],
      hasMore: true
    })
    this.loadMajors()
  },

  selectCategory(e) {
    const category = e.currentTarget.dataset.category
    this.setData({
      selectedCategory: category,
      selectedSubCategory: '',
      page: 1,
      majors: [],
      hasMore: true
    })
    this.loadSubCategories()
    this.loadMajors()
  },

  selectSubCategory(e) {
    const sub = e.currentTarget.dataset.sub
    this.setData({
      selectedSubCategory: sub,
      page: 1,
      majors: [],
      hasMore: true
    })
    this.loadMajors()
  },

  loadCategories() {
    wx.request({
      url: `${app.globalData.baseUrl}/api/major/categories`,
      success: (res) => {
        if (res.data.code === 200 && res.data.data.length > 0) {
          this.setData({ categories: res.data.data })
        }
      }
    })
  },

  loadSubCategories() {
    if (!this.data.selectedCategory) {
      this.setData({ subCategories: [] })
      return
    }

    wx.request({
      url: `${app.globalData.baseUrl}/api/major/sub-categories`,
      data: { category: this.data.selectedCategory },
      success: (res) => {
        if (res.data.code === 200) {
          this.setData({ subCategories: res.data.data || [] })
        }
      }
    })
  },

  loadMajors() {
    if (this.data.loading || !this.data.hasMore) {
      return
    }

    this.setData({ loading: true })

    const params = {
      page: this.data.page,
      size: this.data.size,
      degreeLevel: this.data.degreeLevel
    }

    if (this.data.selectedCategory) {
      params.category = this.data.selectedCategory
    }
    if (this.data.selectedSubCategory) {
      params.subCategory = this.data.selectedSubCategory
    }

    wx.request({
      url: `${app.globalData.baseUrl}/api/major/list`,
      data: params,
      success: (res) => {
        if (res.data.code === 200) {
          const data = res.data.data
          this.setData({
            majors: this.data.majors.concat(data.list),
            hasMore: data.page < data.totalPages,
            loading: false
          })
        } else {
          wx.showToast({
            title: res.data.message || '加载失败',
            icon: 'none'
          })
          this.setData({ loading: false })
        }
      },
      fail: () => {
        wx.showToast({
          title: '网络错误',
          icon: 'none'
        })
        this.setData({ loading: false })
      }
    })
  },

  loadMore() {
    if (this.data.hasMore && !this.data.loading) {
      this.setData({
        page: this.data.page + 1
      })
      this.loadMajors()
    }
  },

  goToDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/major-detail/major-detail?id=${id}`
    })
  },

  goToTest() {
    wx.showToast({
      title: '专业测评功能开发中',
      icon: 'none'
    })
  }
})
