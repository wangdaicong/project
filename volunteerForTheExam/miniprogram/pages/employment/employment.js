const app = getApp();

Page({
  data: {
    searchKeyword: '',
    majorData: null,
    employmentData: {},
    careers: [],
    hotMajors: [],
    industryDistribution: []
  },

  onLoad(options) {
    if (options.majorId) {
      this.loadMajorEmployment(options.majorId);
    }
    this.loadHotMajors();
  },

  loadMajorEmployment(majorId) {
    wx.showLoading({ title: '加载中...' });
    
    wx.request({
      url: `${app.globalData.apiUrl}/api/employment/major/${majorId}`,
      method: 'GET',
      success: (res) => {
        if (res.data.success) {
          const data = res.data.data;
          const distribution = this.processIndustryDistribution(data.industryDistribution);
          this.setData({
            employmentData: data,
            industryDistribution: distribution
          });
          this.loadCareers(majorId);
        } else {
          wx.showToast({
            title: res.data.message || '加载失败',
            icon: 'none'
          });
        }
      },
      fail: () => {
        wx.showToast({
          title: '网络错误',
          icon: 'none'
        });
      },
      complete: () => {
        wx.hideLoading();
      }
    });
  },

  processIndustryDistribution(distribution) {
    if (!distribution) return [];
    const total = Object.values(distribution).reduce((sum, val) => sum + val, 0);
    return Object.entries(distribution).map(([name, value]) => ({
      name: name,
      value: value,
      percentage: total > 0 ? Math.round((value / total) * 100) : 0
    }));
  },

  loadCareers(majorId) {
    wx.request({
      url: `${app.globalData.apiUrl}/api/employment/careers/by-major/${majorId}`,
      method: 'GET',
      success: (res) => {
        if (res.data.success) {
          this.setData({
            careers: res.data.data
          });
        }
      }
    });
  },

  loadHotMajors() {
    wx.request({
      url: `${app.globalData.apiUrl}/api/recommendation/by-employment`,
      method: 'POST',
      data: {
        minSalary: 8000
      },
      success: (res) => {
        if (res.data.success) {
          const majors = res.data.data.slice(0, 10).map(item => ({
            id: item.major.id,
            name: item.major.name,
            avgSalary: item.employment.avgSalary,
            employmentRate: item.employment.employmentRate
          }));
          this.setData({
            hotMajors: majors
          });
        }
      }
    });
  },


  onSearchInput(e) {
    this.setData({
      searchKeyword: e.detail.value
    });
  },

  onSearch() {
    const keyword = this.data.searchKeyword.trim();
    if (!keyword) {
      wx.showToast({
        title: '请输入专业名称',
        icon: 'none'
      });
      return;
    }

    wx.navigateTo({
      url: `/pages/search/search?keyword=${keyword}&type=major`
    });
  },

  onMajorTap(e) {
    const majorId = e.currentTarget.dataset.id;
    this.loadMajorEmployment(majorId);
  },

  onCareerTap(e) {
    const careerId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/career-detail/career-detail?id=${careerId}`
    });
  }
});
