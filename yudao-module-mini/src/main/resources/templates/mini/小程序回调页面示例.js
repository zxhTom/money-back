// pages/callback/callback.js
// 小程序回调页面 - 接收 webview 消息

Page({
  /**
   * 页面的初始数据
   */
  data: {
    webviewUrl: '', // webview 加载的 URL
    loading: true,   // 是否正在加载
    error: false     // 是否加载错误
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    console.log('回调页面 onLoad, 参数:', options);
    
    // 构建回调 URL
    // 从 options 中获取参数，或者使用默认值
    const status = options.status || 'success';
    const idCard = options.idCard || '';
    const verifyToken = options.verifyToken || '';
    
    // 你的服务器域名
    const baseUrl = 'https://your-domain.com/api/mini/callback';
    
    // 构建完整的 URL
    const params = new URLSearchParams({
      status: status,
      idCard: idCard,
      verifyToken: verifyToken
    });
    
    const fullUrl = `${baseUrl}?${params.toString()}`;
    
    console.log('webview URL:', fullUrl);
    
    this.setData({
      webviewUrl: fullUrl,
      loading: true
    });
  },

  /**
   * 接收 webview 发送的消息
   * 这是关键方法，当 webview 调用 postMessage 并返回小程序时会触发
   */
  handleWebViewMessage(e) {
    console.log('收到 webview 消息:', e);
    console.log('消息详情:', JSON.stringify(e.detail, null, 2));
    
    // 消息格式：{ data: [{ status, idNo, verifyToken }] }
    const messages = e.detail.data || [];
    
    if (messages.length === 0) {
      console.warn('消息数组为空');
      wx.showToast({
        title: '未收到有效消息',
        icon: 'none',
        duration: 2000
      });
      return;
    }
    
    // 获取第一条消息（通常只有一条）
    const messageData = messages[0];
    console.log('解析后的消息数据:', messageData);
    
    if (!messageData) {
      console.warn('消息数据为空');
      wx.showToast({
        title: '消息格式错误',
        icon: 'none',
        duration: 2000
      });
      return;
    }
    
    // 提取数据
    const { status, idNo, verifyToken } = messageData;
    
    // 验证必要字段
    if (!status) {
      console.error('缺少 status 字段');
      wx.showToast({
        title: '消息缺少必要字段',
        icon: 'none',
        duration: 2000
      });
      return;
    }
    
    // 根据状态处理
    this.handleCallbackResult(status, idNo, verifyToken);
  },

  /**
   * 处理回调结果
   * @param {string} status - 状态：success 或 failed
   * @param {string} idNo - 身份证号或其他标识
   * @param {string} verifyToken - 验证令牌
   */
  handleCallbackResult(status, idNo, verifyToken) {
    console.log('处理回调结果:', { status, idNo, verifyToken });
    
    if (status === 'success') {
      // 成功处理
      this.handleSuccess(idNo, verifyToken);
    } else {
      // 失败处理
      this.handleFailed(idNo, verifyToken);
    }
  },

  /**
   * 处理成功情况
   */
  handleSuccess(idNo, verifyToken) {
    console.log('处理成功:', { idNo, verifyToken });
    
    // 显示成功提示
    wx.showToast({
      title: '操作成功',
      icon: 'success',
      duration: 2000
    });
    
    // 可以在这里更新页面数据、调用接口等
    // 例如：更新用户验证状态
    // this.updateUserVerifyStatus(idNo, true);
    
    // 延迟后返回上一页或跳转到指定页面
    setTimeout(() => {
      // 方式1: 返回上一页
      const pages = getCurrentPages();
      if (pages.length > 1) {
        wx.navigateBack({
          delta: 1,
          success: () => {
            console.log('返回上一页成功');
          },
          fail: (err) => {
            console.error('返回上一页失败:', err);
            // 如果返回失败，可以跳转到首页
            this.navigateToHome();
          }
        });
      } else {
        // 如果没有上一页，跳转到首页
        this.navigateToHome();
      }
    }, 1500);
  },

  /**
   * 处理失败情况
   */
  handleFailed(idNo, verifyToken) {
    console.log('处理失败:', { idNo, verifyToken });
    
    // 显示失败提示
    wx.showModal({
      title: '操作失败',
      content: '操作未完成，请重试',
      showCancel: true,
      confirmText: '重试',
      cancelText: '返回',
      success: (res) => {
        if (res.confirm) {
          // 用户点击重试，重新加载页面
          this.reloadPage();
        } else if (res.cancel) {
          // 用户点击返回
          wx.navigateBack({
            delta: 1
          });
        }
      }
    });
  },

  /**
   * 更新用户验证状态（示例）
   * 你可以根据实际需求调用你的 API
   */
  updateUserVerifyStatus(idCard, verified) {
    // 调用你的后端 API 更新验证状态
    wx.request({
      url: 'https://your-domain.com/api/user/updateVerify',
      method: 'POST',
      data: {
        idCard: idCard,
        verified: verified
      },
      success: (res) => {
        console.log('更新验证状态成功:', res);
      },
      fail: (err) => {
        console.error('更新验证状态失败:', err);
      }
    });
  },

  /**
   * 跳转到首页
   */
  navigateToHome() {
    wx.switchTab({
      url: '/pages/index/index',
      fail: () => {
        // 如果 switchTab 失败，尝试使用 reLaunch
        wx.reLaunch({
          url: '/pages/index/index'
        });
      }
    });
  },

  /**
   * 重新加载页面
   */
  reloadPage() {
    const currentUrl = this.data.webviewUrl;
    this.setData({
      webviewUrl: '',
      loading: true
    });
    
    // 延迟后重新设置 URL，触发重新加载
    setTimeout(() => {
      this.setData({
        webviewUrl: currentUrl
      });
    }, 100);
  },

  /**
   * 处理 webview 加载错误
   */
  handleWebViewError(e) {
    console.error('webview 加载错误:', e.detail);
    this.setData({
      loading: false,
      error: true
    });
    
    wx.showToast({
      title: '页面加载失败',
      icon: 'none',
      duration: 2000
    });
  },

  /**
   * webview 加载完成
   */
  handleWebViewLoad(e) {
    console.log('webview 加载完成:', e.detail);
    this.setData({
      loading: false
    });
  },

  /**
   * 手动返回按钮（可选）
   */
  handleManualBack() {
    wx.navigateBack({
      delta: 1
    });
  }
});

