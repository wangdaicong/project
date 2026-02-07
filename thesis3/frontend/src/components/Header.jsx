import React from 'react';
import { FileText, User, LogOut, Lightbulb, PenTool, HelpCircle, Sparkles, FileCheck, Presentation } from 'lucide-react';

function Header({ user, onLoginClick, onLogout, currentPage, onNavigate }) {
  return (
    <header className="bg-white shadow-sm sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 py-3 flex items-center justify-between">
        <div 
          className="flex items-center space-x-3 cursor-pointer"
          onClick={() => onNavigate && onNavigate('main')}
        >
          <div className="w-10 h-10 bg-gradient-to-r from-blue-500 to-purple-600 rounded-xl flex items-center justify-center">
            <FileText className="w-6 h-6 text-white" />
          </div>
          <div>
            <h1 className="text-xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
              Easy AI
            </h1>
            <p className="text-xs text-gray-500">AI论文写作平台</p>
          </div>
        </div>

        <nav className="hidden md:flex items-center space-x-6">
          <button 
            onClick={() => onNavigate && onNavigate('topic')}
            className={`flex items-center space-x-1 transition-colors ${
              currentPage === 'topic' ? 'text-blue-600 font-medium' : 'text-gray-600 hover:text-blue-600'
            }`}
          >
            <Lightbulb className="w-4 h-4" />
            <span>论文选题</span>
          </button>
          <button 
            onClick={() => onNavigate && onNavigate('main')}
            className={`flex items-center space-x-1 transition-colors ${
              currentPage === 'main' ? 'text-blue-600 font-medium' : 'text-gray-600 hover:text-blue-600'
            }`}
          >
            <PenTool className="w-4 h-4" />
            <span>论文写作</span>
          </button>
          <button 
            onClick={() => onNavigate && onNavigate('aigc')}
            className={`flex items-center space-x-1 transition-colors ${
              currentPage === 'aigc' ? 'text-blue-600 font-medium' : 'text-gray-600 hover:text-blue-600'
            }`}
          >
            <Sparkles className="w-4 h-4" />
            <span>降AIGC|查重率</span>
          </button>
          <button 
            onClick={() => onNavigate && onNavigate('ppt')}
            className={`flex items-center space-x-1 transition-colors ${
              currentPage === 'ppt' ? 'text-orange-600 font-medium' : 'text-gray-600 hover:text-orange-600'
            }`}
          >
            <Presentation className="w-4 h-4" />
            <span>答辩PPT</span>
          </button>
        </nav>

        <div className="flex items-center space-x-4">
          {user ? (
            <div className="flex items-center space-x-3">
              <div className="flex items-center space-x-2 bg-gray-100 px-4 py-2 rounded-lg">
                <User className="w-5 h-5 text-gray-600" />
                <span className="text-gray-700 font-medium">{user.username}</span>
              </div>
              <button 
                onClick={onLogout}
                className="p-2 text-gray-500 hover:text-red-500 transition-colors"
                title="退出登录"
              >
                <LogOut className="w-5 h-5" />
              </button>
            </div>
          ) : (
            <button 
              onClick={onLoginClick}
              className="btn-primary"
            >
              登录
            </button>
          )}
        </div>
      </div>
    </header>
  );
}

export default Header;
