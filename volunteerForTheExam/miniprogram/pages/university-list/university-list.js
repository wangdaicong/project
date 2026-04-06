const app = getApp()

Page({
  data: {
    keyword: '',
    selectedProvince: '',
    selectedLevel: '',
    selectedType: '',
    
    universities: [],
    total: 0,
    page: 1,
    size: 10,
    loading: false,
    hasMore: true,
    
    compareList: [],
    
    showProvinceModal: false,
    showLevelModal: false,
    showTypeModal: false,
    
    provinces: [
      '北京', '上海', '天津', '重庆',
      '河北', '山西', '辽宁', '吉林', '黑龙江',
      '江苏', '浙江', '安徽', '福建', '江西', '山东',
      '河南', '湖北', '湖南', '广东', '海南',
      '四川', '贵州', '云南', '陕西', '甘肃', '青海',
      '台湾', '内蒙古', '广西', '西藏', '宁夏', '新疆', '香港', '澳门'
    ],
    
    levels: ['985', '211', '双一流'],
    
    types: ['综合', '理工', '师范', '农林', '医药', '财经', '政法', '语言', '艺术', '体育', '民族', '军事']
  },

  onLoad(options) {
    this.loadUniversities()
  },

  onKeywordInput(e) {
    this.setData({
      keyword: e.detail.value
    })
  },

  onSearch() {
    this.setData({
      page: 1,
      universities: [],
      hasMore: true
    })
    this.loadUniversities()
  },

  loadUniversities() {
    if (this.data.loading || !this.data.hasMore) {
      return
    }

    this.setData({ loading: true })

    const params = {
      page: this.data.page,
      size: this.data.size
    }

    if (this.data.keyword) {
      params.keyword = this.data.keyword
    }
    if (this.data.selectedProvince) {
      params.province = this.data.selectedProvince
    }
    if (this.data.selectedLevel) {
      params.level = this.data.selectedLevel
    }
    if (this.data.selectedType) {
      params.type = this.data.selectedType
    }

    wx.request({
      url: `${app.globalData.baseUrl}/api/university/list`,
      data: params,
      success: (res) => {
        if (res.data.code === 200) {
          const data = res.data.data
          this.setData({
            universities: this.data.universities.concat(data.list),
            total: data.total,
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
      this.loadUniversities()
    }
  },

  goToDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/university-detail/university-detail?id=${id}`
    })
  },

  toggleCompare(e) {
    const id = e.currentTarget.dataset.id
    let compareList = this.data.compareList
    
    if (compareList.includes(id)) {
      compareList = compareList.filter(item => item !== id)
    } else {
      if (compareList.length >= 4) {
        wx.showToast({
          title: '最多对比4所院校',
          icon: 'none'
        })
        return
      }
      compareList.push(id)
    }
    
    this.setData({ compareList })
  },

  clearCompare() {
    this.setData({ compareList: [] })
  },

  goToCompare() {
    if (this.data.compareList.length < 2) {
      wx.showToast({
        title: '至少选择2所院校',
        icon: 'none'
      })
      return
    }
    
    wx.navigateTo({
      url: `/pages/university-compare/university-compare?ids=${this.data.compareList.join(',')}`
    })
  },

  showProvinceFilter() {
    this.setData({ showProvinceModal: true })
  },

  hideProvinceFilter() {
    this.setData({ showProvinceModal: false })
  },

  selectProvince(e) {
    const province = e.currentTarget.dataset.province
    this.setData({
      selectedProvince: province,
      showProvinceModal: false,
      page: 1,
      universities: [],
      hasMore: true
    })
    this.loadUniversities()
  },

  showLevelFilter() {
    this.setData({ showLevelModal: true })
  },

  hideLevelFilter() {
    this.setData({ showLevelModal: false })
  },

  selectLevel(e) {
    const level = e.currentTarget.dataset.level
    this.setData({
      selectedLevel: level,
      showLevelModal: false,
      page: 1,
      universities: [],
      hasMore: true
    })
    this.loadUniversities()
  },

  showTypeFilter() {
    this.setData({ showTypeModal: true })
  },

  hideTypeFilter() {
    this.setData({ showTypeModal: false })
  },

  selectType(e) {
    const type = e.currentTarget.dataset.type
    this.setData({
      selectedType: type,
      showTypeModal: false,
      page: 1,
      universities: [],
      hasMore: true
    })
    this.loadUniversities()
  },

  stopPropagation() {
    // 阻止事件冒泡
  }
})
