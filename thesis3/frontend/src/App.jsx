import React, { useState } from 'react';
import { Toaster } from 'react-hot-toast';
import Header from './components/Header';
import Sidebar from './components/Sidebar';
import MainContent from './components/MainContent';
import LoginModal from './components/LoginModal';

function App() {
  const [user, setUser] = useState(null);
  const [showLogin, setShowLogin] = useState(false);
  const [activeCategory, setActiveCategory] = useState('论文专区');
  const [activePaperType, setActivePaperType] = useState('毕业论文');

  return (
    <div className="min-h-screen">
      <Toaster position="top-center" />
      <Header 
        user={user} 
        onLoginClick={() => setShowLogin(true)} 
        onLogout={() => setUser(null)}
      />
      
      <div className="flex">
        <Sidebar 
          activeCategory={activeCategory}
          setActiveCategory={setActiveCategory}
          activePaperType={activePaperType}
          setActivePaperType={setActivePaperType}
        />
        
        <MainContent 
          user={user}
          paperType={activePaperType}
          onLoginRequired={() => setShowLogin(true)}
        />
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
