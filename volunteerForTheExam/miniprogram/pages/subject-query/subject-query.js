const app = getApp()

Page({
  data: {
    activeTab: 0,
    years: ['2027', '2026', '2025', '2024'],
    yearIndex: 3,
    provinces: ['广东', '北京', '上海', '浙江', '江苏', '山东', '河南', '湖北', '湖南', '四川', 
                '重庆', '天津', '河北', '山西', '内蒙古', '辽宁', '吉林', '黑龙江', '安徽', '福建',
                '江西', '湖南', '广西', '海南', '贵州', '云南', '西藏', '陕西', '甘肃', '青海', '宁夏', '新疆'],
    provinceIndex: 0,
    subjects: [
      { name: '物理', selected: false },
      { name: '化学', selected: false },
      { name: '生物', selected: false },
      { name: '政治', selected: false },
      { name: '历史', selected: false },
      { name: '地理', selected: false }
    ],
    selectedSubjectsText: '',
    universityType: '',
    degreeLevels: ['本科', '专科'],
    degreeLevelIndex: 0,
    majorCategories: [],
    majorCategoryIndex: 0,
    majors: [],
    majorIndex: 0,
    universityName: '',
    canApplyResults: [],
    cannotApplyResults: [],
    canApplyCount: 0,
    cannotApplyCount: 0,
    categoryCount: {},
    showCanApply: true,
    queried: false
  },

  onLoad() {
    this.loadMajorCategories()
  },

  openOfficialSite() {
    wx.showModal({
      title: '官方查询系统',
      content: '即将跳转到广东省教育考试院官方选科查询系统，请在浏览器中打开：\n\nhttps://www.eeagd.edu.cn/xkcx2024/',
      confirmText: '复制链接',
      success: (res) => {
        if (res.confirm) {
          wx.setClipboardData({
            data: 'https://www.eeagd.edu.cn/xkcx2024/',
            success: () => {
              wx.showToast({
                title: '链接已复制',
                icon: 'success'
              })
            }
          })
        }
      }
    })
  },

  switchTab(e) {
    const index = e.currentTarget.dataset.index
    this.setData({
      activeTab: index,
      canApplyResults: [],
      cannotApplyResults: [],
      queried: false
    })
  },

  toggleResultType(e) {
    const type = e.currentTarget.dataset.type
    this.setData({
      showCanApply: type === 'can'
    })
  },

  onYearChange(e) {
    this.setData({
      yearIndex: e.detail.value
    })
  },

  onProvinceChange(e) {
    this.setData({
      provinceIndex: e.detail.value
    })
  },

  toggleSubject(e) {
    const index = e.currentTarget.dataset.index
    const subjects = this.data.subjects
    const selectedCount = subjects.filter(s => s.selected).length

    if (!subjects[index].selected && selectedCount >= 3) {
      wx.showToast({
        title: '最多选择3门科目',
        icon: 'none'
      })
      return
    }

    subjects[index].selected = !subjects[index].selected
    const selectedSubjects = subjects.filter(s => s.selected).map(s => s.name)
    
    this.setData({
      subjects,
      selectedSubjectsText: selectedSubjects.join('+')
    })
  },

  selectUniversityType(e) {
    this.setData({
      universityType: e.currentTarget.dataset.type
    })
  },

  onDegreeLevelChange(e) {
    this.setData({
      degreeLevelIndex: e.detail.value
    })
  },

  onMajorCategoryChange(e) {
    const index = e.detail.value
    this.setData({
      majorCategoryIndex: index
    })
    this.loadMajorsByCategory(this.data.majorCategories[index])
  },

  onMajorChange(e) {
    this.setData({
      majorIndex: e.detail.value
    })
  },

  onUniversityNameInput(e) {
    this.setData({
      universityName: e.detail.value
    })
  },

  loadMajorCategories() {
    wx.request({
      url: `${app.globalData.apiUrl}/api/subject-requirement/major-categories`,
      success: (res) => {
        if (res.data.success) {
          this.setData({
            majorCategories: res.data.data
          })
        }
      }
    })
  },

  loadMajorsByCategory(category) {
    wx.request({
      url: `${app.globalData.apiUrl}/api/subject-requirement/majors-by-category`,
      data: { category },
      success: (res) => {
        if (res.data.success) {
          this.setData({
            majors: res.data.data,
            majorIndex: 0
          })
        }
      }
    })
  },

  queryBySubjects() {
    const selectedSubjects = this.data.subjects.filter(s => s.selected)
    
    if (selectedSubjects.length !== 3) {
      wx.showToast({
        title: '请选择3门科目',
        icon: 'none'
      })
      return
    }

    const subjects = selectedSubjects.map(s => s.name).join(',')
    const province = this.data.provinces[this.data.provinceIndex]
    const year = this.data.years[this.data.yearIndex]

    wx.showLoading({ title: '查询中...' })

    wx.request({
      url: `${app.globalData.apiUrl}/api/subject-requirement/query-by-subjects`,
      data: {
        subjects,
        province,
        year,
        universityType: this.data.universityType
      },
      success: (res) => {
        wx.hideLoading()
        if (res.data.success) {
          this.setData({
            results: res.data.data,
            queried: true
          })
        } else {
          wx.showToast({
            title: res.data.message || '查询失败',
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

  queryByMajor() {
    if (!this.data.majors[this.data.majorIndex]) {
      wx.showToast({
        title: '请选择专业',
        icon: 'none'
      })
      return
    }

    const majorName = this.data.majors[this.data.majorIndex]
    const province = this.data.provinces[this.data.provinceIndex]
    const year = this.data.years[this.data.yearIndex]

    wx.showLoading({ title: '查询中...' })

    wx.request({
      url: `${app.globalData.apiUrl}/api/subject-requirement/query-by-major`,
      data: { majorName, province, year },
      success: (res) => {
        wx.hideLoading()
        if (res.data.success) {
          this.setData({
            results: res.data.data,
            queried: true
          })
        } else {
          wx.showToast({
            title: res.data.message || '查询失败',
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

  queryByUniversity() {
    if (!this.data.universityName.trim()) {
      wx.showToast({
        title: '请输入院校名称',
        icon: 'none'
      })
      return
    }

    const province = this.data.provinces[this.data.provinceIndex]
    const year = this.data.years[this.data.yearIndex]

    wx.showLoading({ title: '查询中...' })

    wx.request({
      url: `${app.globalData.apiUrl}/api/subject-requirement/query-by-university`,
      data: {
        universityName: this.data.universityName,
        province,
        year
      },
      success: (res) => {
        wx.hideLoading()
        if (res.data.success) {
          this.setData({
            results: res.data.data,
            queried: true
          })
        } else {
          wx.showToast({
            title: res.data.message || '查询失败',
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
  }
})
