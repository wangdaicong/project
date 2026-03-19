const app = getApp();

Page({
  data: {
    selectedTier: '',
    cities: [],
    selectedCities: [],
    showCompareModal: false
  },

  onLoad() {
    this.loadCities();
  },

  loadCities(tier) {
    wx.showLoading({ title: '加载中...' });
    
    const url = tier 
      ? `${app.globalData.apiUrl}/api/employment/cities?tier=${tier}`
      : `${app.globalData.apiUrl}/api/employment/cities`;

    wx.request({
      url: url,
      method: 'GET',
      success: (res) => {
        if (res.data.success) {
          const cities = res.data.data.map(item => ({
            ...item,
            hotIndustriesArray: this.parseJSON(item.hotIndustries, []),
            selected: false
          }));
          this.setData({
            cities: cities
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

  parseJSON(str, defaultValue) {
    try {
      return JSON.parse(str);
    } catch (e) {
      return defaultValue;
    }
  },

  onTierSelect(e) {
    const tier = e.currentTarget.dataset.tier;
    this.setData({
      selectedTier: tier,
      selectedCities: []
    });
    this.loadCities(tier);
  },

  onCityTap(e) {
    const cityId = e.currentTarget.dataset.id;
    console.log('City tapped:', cityId);
  },

  onCompareChange(e) {
    const cityId = e.detail.value[0];
    const city = e.currentTarget.dataset.city;
    
    let selectedCities = [...this.data.selectedCities];
    const index = selectedCities.findIndex(item => item.id === city.id);
    
    if (index > -1) {
      selectedCities.splice(index, 1);
    } else {
      if (selectedCities.length >= 4) {
        wx.showToast({
          title: '最多选择4个城市对比',
          icon: 'none'
        });
        return;
      }
      selectedCities.push(city);
    }

    const cities = this.data.cities.map(item => ({
      ...item,
      selected: selectedCities.some(c => c.id === item.id)
    }));

    this.setData({
      cities: cities,
      selectedCities: selectedCities
    });
  },

  showComparison() {
    if (this.data.selectedCities.length < 2) {
      wx.showToast({
        title: '请至少选择2个城市',
        icon: 'none'
      });
      return;
    }

    this.setData({
      showCompareModal: true
    });
  },

  calculateCostPerformance(city) {
    const ratio = (city.avgSalary / city.livingCost).toFixed(2);
    if (ratio >= 2) return '很高';
    if (ratio >= 1.5) return '较高';
    if (ratio >= 1) return '一般';
    return '较低';
  },

  closeComparison() {
    this.setData({
      showCompareModal: false
    });
  },

  stopPropagation() {
  }
});
