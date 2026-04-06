const app = getApp()

Page({
  data: {
    universityId: 0,
    university: {},
    currentTab: 0,
    isCollected: false,
    showFullIntro: false,
    
    majors: [],
    allMajors: [],
    selectedCategory: '',
    categories: ['工学', '医学', '文学', '管理学', '理学', '经济学', '法学', '教育学', '艺术学', '农学', '历史学', '哲学']
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ universityId: options.id })
      this.loadUniversityDetail()
      this.loadMajors()
    }
  },

  loadUniversityDetail() {
    wx.showLoading({ title: '加载中...' })
    
    wx.request({
      url: `${app.globalData.baseUrl}/api/university/detail/${this.data.universityId}`,
      success: (res) => {
        wx.hideLoading()
        if (res.data.code === 200) {
          this.setData({ university: res.data.data })
        } else {
          wx.showToast({
            title: res.data.message || '加载失败',
            icon: 'none'
          })
        }
      },
      fail: () => {
        wx.hideLoading()
        wx.showToast({
          title: '网络错误',
          icon: 'none'
        })
      }
    })
  },

  loadMajors() {
    const params = {}
    if (this.data.selectedCategory) {
      params.category = this.data.selectedCategory
    }

    wx.request({
      url: `${app.globalData.baseUrl}/api/university/${this.data.universityId}/majors`,
      data: params,
      success: (res) => {
        if (res.data.code === 200) {
          const majorsData = res.data.data.majors || []
          this.setData({ 
            majors: majorsData,
            allMajors: this.data.selectedCategory === '' ? majorsData : this.data.allMajors
          })
          
          // 提取实际存在的学科门类
          if (this.data.selectedCategory === '') {
            const categorySet = new Set()
            majorsData.forEach(major => {
              if (major.category) {
                categorySet.add(major.category)
              }
            })
            if (categorySet.size > 0) {
              this.setData({ categories: Array.from(categorySet).sort() })
            }
          }
        }
      }
    })
  },

  switchTab(e) {
    const index = e.currentTarget.dataset.index
    this.setData({ currentTab: index })
    
    // 切换到开设专业Tab时加载数据
    if (index === 1 && this.data.majors.length === 0) {
      this.loadMajors()
    }
  },

  selectCategory(e) {
    const category = e.currentTarget.dataset.category
    this.setData({ 
      selectedCategory: category
    })
    this.loadMajors()
  },

  toggleCollect() {
    this.setData({ isCollected: !this.data.isCollected })
    wx.showToast({
      title: this.data.isCollected ? '收藏成功' : '取消收藏',
      icon: 'success'
    })
  },

  toggleIntro() {
    this.setData({ showFullIntro: !this.data.showFullIntro })
  },

  openWebsite() {
    if (this.data.university.website) {
      wx.setClipboardData({
        data: this.data.university.website,
        success: () => {
          wx.showToast({
            title: '网址已复制',
            icon: 'success'
          })
        }
      })
    }
  },

  goToMajorDetail(e) {
    const id = e.currentTarget.dataset.id
    if (id) {
      wx.navigateTo({
        url: `/pages/major-detail/major-detail?id=${id}`
      })
    } else {
      wx.showToast({
        title: '专业详情开发中',
        icon: 'none'
      })
    }
  }
})
