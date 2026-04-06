const request = require('../../../utils/request');
const app = getApp();

Page({
  data: {
    universityId: null,
    universityName: '',
    province: '北京',
    category: '理科',
    categories: ['理科', '文科'],
    categoryIndex: 0,
    
    // 图表数据
    chartData: null,
    years: [],
    minScores: [],
    avgScores: [],
    maxScores: [],
    minRanks: [],
    
    // 详细数据
    scoreList: [],
    
    // 加载状态
    loading: false,
    hasData: false
  },

  onLoad(options) {
    if (options.id) {
      this.setData({
        universityId: options.id,
        universityName: options.name || '院校'
      });
      
      wx.setNavigationBarTitle({
        title: `${options.name || '院校'}分数线趋势`
      });
      
      // 从全局获取省份
      const userProvince = app.globalData.userProvince;
      if (userProvince) {
        this.setData({ province: userProvince });
      }
      
      this.loadScoreTrend();
    }
  },

  // 切换科类
  onCategoryChange(e) {
    const index = e.detail.value;
    this.setData({
      categoryIndex: index,
      category: this.data.categories[index]
    });
    this.loadScoreTrend();
  },

  // 加载分数线趋势
  loadScoreTrend() {
    this.setData({ loading: true });
    
    const { universityId, province, category } = this.data;
    
    request.get(`/score-line/trend/${universityId}`, {
      province,
      category
    }).then(res => {
      console.log('分数线趋势数据:', res);
      
      if (res && res.years && res.years.length > 0) {
        // 反转数据，使年份从旧到新
        const years = [...res.years].reverse();
        const minScores = [...res.minScores].reverse();
        const avgScores = [...res.avgScores].reverse();
        const maxScores = [...res.maxScores].reverse();
        const minRanks = [...res.minRanks].reverse();
        
        this.setData({
          years,
          minScores,
          avgScores,
          maxScores,
          minRanks,
          scoreList: res.data || [],
          hasData: true,
          loading: false
        });
        
        // 绘制图表
        this.drawChart();
      } else {
        this.setData({
          hasData: false,
          loading: false
        });
        wx.showToast({
          title: '暂无数据',
          icon: 'none'
        });
      }
    }).catch(err => {
      console.error('加载分数线失败:', err);
      this.setData({
        hasData: false,
        loading: false
      });
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      });
    });
  },

  // 绘制图表
  drawChart() {
    const { years, minScores, avgScores, maxScores } = this.data;
    
    // 使用微信小程序Canvas绘制简单图表
    const ctx = wx.createCanvasContext('scoreChart', this);
    const canvasWidth = 710;
    const canvasHeight = 400;
    const padding = 50;
    const chartWidth = canvasWidth - padding * 2;
    const chartHeight = canvasHeight - padding * 2;
    
    // 清空画布
    ctx.clearRect(0, 0, canvasWidth, canvasHeight);
    
    // 绘制背景
    ctx.setFillStyle('#ffffff');
    ctx.fillRect(0, 0, canvasWidth, canvasHeight);
    
    // 计算数据范围
    const allScores = [...minScores, ...avgScores, ...maxScores];
    const minValue = Math.min(...allScores) - 10;
    const maxValue = Math.max(...allScores) + 10;
    const valueRange = maxValue - minValue;
    
    // 绘制网格线和Y轴标签
    ctx.setStrokeStyle('#e0e0e0');
    ctx.setLineWidth(1);
    ctx.setFontSize(12);
    ctx.setFillStyle('#999');
    
    for (let i = 0; i <= 5; i++) {
      const y = padding + (chartHeight / 5) * i;
      const value = Math.round(maxValue - (valueRange / 5) * i);
      
      // 绘制横线
      ctx.beginPath();
      ctx.moveTo(padding, y);
      ctx.lineTo(canvasWidth - padding, y);
      ctx.stroke();
      
      // 绘制Y轴标签
      ctx.fillText(value.toString(), 10, y + 5);
    }
    
    // 绘制X轴标签
    const xStep = chartWidth / (years.length - 1);
    years.forEach((year, index) => {
      const x = padding + xStep * index;
      ctx.fillText(year.toString(), x - 15, canvasHeight - 20);
    });
    
    // 绘制分数线
    const drawLine = (scores, color, lineWidth = 2) => {
      ctx.setStrokeStyle(color);
      ctx.setLineWidth(lineWidth);
      ctx.beginPath();
      
      scores.forEach((score, index) => {
        const x = padding + xStep * index;
        const y = padding + chartHeight - ((score - minValue) / valueRange) * chartHeight;
        
        if (index === 0) {
          ctx.moveTo(x, y);
        } else {
          ctx.lineTo(x, y);
        }
      });
      
      ctx.stroke();
      
      // 绘制数据点
      ctx.setFillStyle(color);
      scores.forEach((score, index) => {
        const x = padding + xStep * index;
        const y = padding + chartHeight - ((score - minValue) / valueRange) * chartHeight;
        ctx.beginPath();
        ctx.arc(x, y, 4, 0, 2 * Math.PI);
        ctx.fill();
      });
    };
    
    // 绘制三条线
    drawLine(minScores, '#52c41a'); // 最低分 - 绿色
    drawLine(avgScores, '#1890ff'); // 平均分 - 蓝色
    drawLine(maxScores, '#ff4d4f'); // 最高分 - 红色
    
    ctx.draw();
  },

  // 查看详细数据
  showDetail(e) {
    const index = e.currentTarget.dataset.index;
    const item = this.data.scoreList[index];
    
    const content = `年份：${item.year}\n` +
                   `最低分：${item.minScore}\n` +
                   `平均分：${item.avgScore}\n` +
                   `最高分：${item.maxScore}\n` +
                   `最低位次：${item.minRank}\n` +
                   `招生人数：${item.enrollmentCount}`;
    
    wx.showModal({
      title: '详细数据',
      content: content,
      showCancel: false
    });
  }
});
