import React, { createContext, useContext, useState } from 'react';
import api from '../api/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('justeat_user');
    return stored ? JSON.parse(stored) : null;
  });

  const login = async (username, password) => {
    const { data } = await api.post('/auth/login', { username, password });
    persist(data);
    return data;
  };

  const register = async (username, email, password, role) => {
    const { data } = await api.post('/auth/register', { username, email, password, role });
    persist(data);
    return data;
  };

  const persist = (data) => {
    const userInfo = { username: data.username, role: data.role, userId: data.userId };
    localStorage.setItem('justeat_token', data.token);
    localStorage.setItem('justeat_user', JSON.stringify(userInfo));
    setUser(userInfo);
  };

  const logout = () => {
    localStorage.removeItem('justeat_token');
    localStorage.removeItem('justeat_user');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
