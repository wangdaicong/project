const app = getApp();

Page({
  data: {
    activeTab: 'industry',
    industries: [],
    careers: [],
    selectedIndustry: null,
    selectedIndustryDetail: null,
    relatedCareers: []
  },

  onLoad() {
    this.loadIndustries();
    this.loadCareers();
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab;
    this.setData({
      activeTab: tab
    });
  },

  loadIndustries() {
    wx.showLoading({ title: '加载中...' });
    
    wx.request({
      url: `${app.globalData.apiUrl}/api/employment/industries`,
      method: 'GET',
      success: (res) => {
        if (res.data.success) {
          const industries = res.data.data.map(item => ({
            ...item,
            hotCitiesArray: this.parseJSON(item.hotCities, [])
          }));
          this.setData({
            industries: industries
          });
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

  loadCareers(industryId) {
    const url = industryId 
      ? `${app.globalData.apiUrl}/api/career/industry?industryId=${industryId}`
      : `${app.globalData.apiUrl}/api/career/hot`;

    wx.request({
      url: url,
      method: 'GET',
      success: (res) => {
        if (res.data.code === 200 || res.data.success) {
          const careers = (res.data.data || []).map(item => ({
            ...item,
            skillRequirementsArray: this.parseJSON(item.skillRequirements, [])
          }));
          this.setData({
            careers: careers
          });
        }
      }
    });
  },

  parseJSON(str, defaultValue) {
    try {
      return JSON.parse(str);
    } catch (e) {
      return defaultValue;
    }
  },

  onIndustryFilter(e) {
    const index = e.detail.value;
    const industry = this.data.industries[index];
    this.setData({
      selectedIndustry: industry
    });
    this.loadCareers(industry.id);
  },

  onIndustryTap(e) {
    const industryId = e.currentTarget.dataset.id;
    const industry = this.data.industries.find(item => item.id === industryId);
    
    wx.showLoading({ title: '加载中...' });
    
    wx.request({
      url: `${app.globalData.apiUrl}/api/employment/industry/${industryId}`,
      method: 'GET',
      success: (res) => {
        if (res.data.success) {
          const careers = (res.data.data.careers || []).map(item => ({
            ...item,
            skillRequirementsArray: this.parseJSON(item.skillRequirements, [])
          }));
          
          this.setData({
            selectedIndustryDetail: industry,
            relatedCareers: careers
          });
        }
      },
      complete: () => {
        wx.hideLoading();
      }
    });
  },

  onCareerTap(e) {
    const careerId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/career-detail/career-detail?id=${careerId}`
    });
  },

  closeDetail() {
    this.setData({
      selectedIndustryDetail: null,
      relatedCareers: []
    });
  },

  stopPropagation() {
  }
});
