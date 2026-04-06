const request = require('../../utils/request');
const app = getApp();

Page({
  data: {
    step: 1,
    studentName: '',
    province: '北京',
    score: '',
    category: '理科',
    rankPosition: '',
    
    provinces: ['北京', '上海', '天津', '重庆', '河北', '山西', '辽宁', '吉林', '黑龙江', 
                '江苏', '浙江', '安徽', '福建', '江西', '山东', '河南', '湖北', '湖南', 
                '广东', '海南', '四川', '贵州', '云南', '陕西', '甘肃', '青海', '台湾', 
                '内蒙古', '广西', '西藏', '宁夏', '新疆', '香港', '澳门'],
    categories: ['理科', '文科'],
    
    applicationId: null,
    volunteers: [],
    recommendations: [],
    selectedUniversities: [],
    
    showRecommendModal: false,
    currentRecommendType: 'all',
    recommendTypes: [
      { value: 'all', label: '全部' },
      { value: 'rush', label: '冲刺' },
      { value: 'stable', label: '稳妥' },
      { value: 'safe', label: '保底' }
    ]
  },

  onLoad(options) {
    if (options.applicationId) {
      this.setData({ applicationId: options.applicationId });
      this.loadApplicationDetail();
    }
  },

  onProvinceChange(e) {
    this.setData({ province: this.data.provinces[e.detail.value] });
  },

  onCategoryChange(e) {
    this.setData({ category: this.data.categories[e.detail.value] });
  },

  onInputChange(e) {
    const { field } = e.currentTarget.dataset;
    this.setData({ [field]: e.detail.value });
  },

  nextStep() {
    const { step, studentName, province, score, category } = this.data;
    
    if (step === 1) {
      if (!studentName || !score) {
        wx.showToast({ title: '请填写完整信息', icon: 'none' });
        return;
      }
      
      if (score < 0 || score > 750) {
        wx.showToast({ title: '分数范围0-750', icon: 'none' });
        return;
      }
      
      this.createApplication();
    } else if (step === 2) {
      if (this.data.selectedUniversities.length === 0) {
        wx.showToast({ title: '请至少选择一个志愿', icon: 'none' });
        return;
      }
      
      this.saveVolunteers();
    }
  },

  prevStep() {
    if (this.data.step > 1) {
      this.setData({ step: this.data.step - 1 });
    }
  },

  async createApplication() {
    wx.showLoading({ title: '创建中...' });
    
    try {
      const res = await request.post('/volunteer/create', {
        studentName: this.data.studentName,
        province: this.data.province,
        score: parseInt(this.data.score),
        category: this.data.category,
        rankPosition: this.data.rankPosition ? parseInt(this.data.rankPosition) : null,
        batch: '本科一批'
      });
      
      if (res.code === 200) {
        this.setData({ 
          applicationId: res.data,
          step: 2
        });
        this.loadRecommendations();
      } else {
        wx.showToast({ title: res.message, icon: 'none' });
      }
    } catch (error) {
      wx.showToast({ title: '创建失败', icon: 'none' });
    } finally {
      wx.hideLoading();
    }
  },

  async loadRecommendations() {
    wx.showLoading({ title: '加载推荐...' });
    
    try {
      const res = await request.get('/volunteer/recommend', {
        province: this.data.province,
        score: parseInt(this.data.score),
        category: this.data.category
      });
      
      if (res.code === 200) {
        this.setData({ recommendations: res.data || [] });
      }
    } catch (error) {
      console.error('加载推荐失败', error);
    } finally {
      wx.hideLoading();
    }
  },

  showRecommendations() {
    this.setData({ showRecommendModal: true });
  },

  hideRecommendModal() {
    this.setData({ showRecommendModal: false });
  },

  onRecommendTypeChange(e) {
    this.setData({ currentRecommendType: e.detail.value });
  },

  selectUniversity(e) {
    const { university, recommendtype } = e.currentTarget.dataset;
    const { selectedUniversities } = this.data;
    
    const exists = selectedUniversities.find(u => u.id === university.id);
    if (exists) {
      wx.showToast({ title: '该院校已添加', icon: 'none' });
      return;
    }
    
    selectedUniversities.push({
      universityId: university.id,
      universityName: university.name,
      riskLevel: recommendtype
    });
    
    this.setData({ selectedUniversities });
    wx.showToast({ title: '添加成功', icon: 'success' });
  },

  removeVolunteer(e) {
    const { index } = e.currentTarget.dataset;
    const { selectedUniversities } = this.data;
    selectedUniversities.splice(index, 1);
    this.setData({ selectedUniversities });
  },

  moveUp(e) {
    const { index } = e.currentTarget.dataset;
    if (index === 0) return;
    
    const { selectedUniversities } = this.data;
    const temp = selectedUniversities[index];
    selectedUniversities[index] = selectedUniversities[index - 1];
    selectedUniversities[index - 1] = temp;
    this.setData({ selectedUniversities });
  },

  moveDown(e) {
    const { index } = e.currentTarget.dataset;
    const { selectedUniversities } = this.data;
    if (index === selectedUniversities.length - 1) return;
    
    const temp = selectedUniversities[index];
    selectedUniversities[index] = selectedUniversities[index + 1];
    selectedUniversities[index + 1] = temp;
    this.setData({ selectedUniversities });
  },

  async saveVolunteers() {
    wx.showLoading({ title: '保存中...' });
    
    try {
      await request.post('/volunteer/save-details', this.data.selectedUniversities, {
        params: { applicationId: this.data.applicationId }
      });
      
      this.analyzeVolunteers();
    } catch (error) {
      wx.hideLoading();
      wx.showToast({ title: '保存失败', icon: 'none' });
    }
  },

  async analyzeVolunteers() {
    try {
      const res = await request.post(`/volunteer/analyze/${this.data.applicationId}`);
      
      wx.hideLoading();
      
      if (res.code === 200) {
        wx.redirectTo({
          url: `/pages/volunteer-result/volunteer-result?applicationId=${this.data.applicationId}`
        });
      } else {
        wx.showToast({ title: res.message, icon: 'none' });
      }
    } catch (error) {
      wx.hideLoading();
      wx.showToast({ title: '分析失败', icon: 'none' });
    }
  },

  async loadApplicationDetail() {
    wx.showLoading({ title: '加载中...' });
    
    try {
      const res = await request.get(`/volunteer/detail/${this.data.applicationId}`);
      
      if (res.code === 200) {
        const { application, volunteers } = res.data;
        this.setData({
          studentName: application.studentName,
          province: application.province,
          score: application.score.toString(),
          category: application.category,
          rankPosition: application.rankPosition ? application.rankPosition.toString() : '',
          selectedUniversities: volunteers || [],
          step: 2
        });
        this.loadRecommendations();
      }
    } catch (error) {
      wx.showToast({ title: '加载失败', icon: 'none' });
    } finally {
      wx.hideLoading();
    }
  }
});
