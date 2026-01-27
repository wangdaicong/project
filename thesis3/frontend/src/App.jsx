import React, { useState } from 'react';
import { Toaster } from 'react-hot-toast';
import Header from './components/Header';
import Sidebar from './components/Sidebar';
import MainContent from './components/MainContent';
import TopicPage from './components/TopicPage';
import HelpPage from './components/HelpPage';
import AigcPage from './components/AigcPage';
import PaperPassPage from './components/PaperPassPage';
import PptPage from './components/PptPage';
import LoginModal from './components/LoginModal';

function App() {
  const [user, setUser] = useState(null);
  const [showLogin, setShowLogin] = useState(false);
  const [activeCategory, setActiveCategory] = useState('论文专区');
  const [activePaperType, setActivePaperType] = useState('毕业论文');
  const [currentPage, setCurrentPage] = useState('main'); // 'main' | 'topic' | 'help' | 'aigc' | 'paperpass' | 'ppt'
  const [selectedTopic, setSelectedTopic] = useState('');

  const handleSelectTopic = (title) => {
    setSelectedTopic(title);
    setCurrentPage('main');
  };

  return (
    <div className="min-h-screen">
      <Toaster
        position="top-center"
        toastOptions={{
          style: {
            whiteSpace: 'nowrap',
            overflow: 'hidden',
            textOverflow: 'ellipsis'
          }
        }}
      />
      <Header 
        user={user} 
        onLoginClick={() => setShowLogin(true)} 
        onLogout={() => setUser(null)}
        currentPage={currentPage}
        onNavigate={setCurrentPage}
      />
      
      <div className="flex">
        <Sidebar 
          activeCategory={activeCategory}
          setActiveCategory={setActiveCategory}
          activePaperType={activePaperType}
          setActivePaperType={setActivePaperType}
        />
        
        {currentPage === 'topic' ? (
          <TopicPage onSelectTopic={handleSelectTopic} />
        ) : currentPage === 'help' ? (
          <HelpPage />
        ) : currentPage === 'aigc' ? (
          <AigcPage />
        ) : currentPage === 'paperpass' ? (
          <PaperPassPage />
        ) : currentPage === 'ppt' ? (
          <PptPage />
        ) : (
          <MainContent 
            user={user}
            paperType={activePaperType}
            onLoginRequired={() => setShowLogin(true)}
            initialTitle={selectedTopic}
            onTitleUsed={() => setSelectedTopic('')}
          />
        )}
      </div>

      {showLogin && (
        <LoginModal 
          onClose={() => setShowLogin(false)}
          onLogin={setUser}
        />
      )}
    </div>
  );
}

export default App;
