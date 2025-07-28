import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, Pressable } from 'react-native';
import { Ionicons, AntDesign, FontAwesome } from '@expo/vector-icons';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useRouter } from 'expo-router';
import { useAuth } from '../../contexts/AuthContext';

const isValidEmail = (email: string) => /\S+@\S+\.\S+/.test(email);

const LoginScreen = () => {
  const router = useRouter();
  const { login, isLoading } = useAuth();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [usernameError, setUsernameError] = useState('');
  const [passwordError, setPasswordError] = useState('');

  const handleLogin = async () => {
    setUsernameError('');
    setPasswordError('');
    let usernameWrong = false;
    let passwordWrong = false;

    if (!username) {
      setUsernameError('Username is required.');
      usernameWrong = true;
    } else if (username.length < 3) {
      setUsernameError('Username must be at least 3 characters.');
      usernameWrong = true;
    }
    if (!password) {
      setPasswordError('Password is required.');
      passwordWrong = true;
    } else if (password.length < 6) {
      setPasswordError('Password must be at least 6 characters.');
      passwordWrong = true;
    }
    if (usernameWrong || passwordWrong) return;

    try {
      await login(username, password);
      router.replace('/(tabs)');
    } catch (error) {
      setPasswordError(error instanceof Error ? error.message : 'Login failed');
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <Text style={styles.title}>
        <Text style={{ color: '#1D1C1D', fontWeight: 'bold' }}>LOG IN TO </Text>
        <Text style={{ color: '#FF6600', fontWeight: 'bold' }}>DEVSYNC</Text>
      </Text>
      {/* Email Label and Input */}
      <Text style={styles.label}>Username</Text>
      <TextInput
        placeholder="Username"
        value={username}
        onChangeText={text => { setUsername(text); setUsernameError(''); }}
        style={[styles.input, usernameError && styles.inputError]}
        autoCapitalize="none"
        placeholderTextColor="#6e6259"
      />
      {usernameError ? <Text style={styles.errorText}>{usernameError}</Text> : null}
      {/* Password Label and Input */}
      <Text style={[styles.label, { marginTop: 18 }]}>Password</Text>
      <View style={styles.passwordContainer}>
        <TextInput
          placeholder="Password"
          value={password}
          onChangeText={text => { setPassword(text); setPasswordError(''); }}
          style={[styles.input, { flex: 1, marginBottom: 0 }, passwordError && styles.inputError]}
          secureTextEntry={!showPassword}
          placeholderTextColor="#6e6259"
        />
        <Pressable onPress={() => setShowPassword(!showPassword)} style={styles.eyeIcon}>
          <Ionicons name={showPassword ? 'eye-off' : 'eye'} size={22} color="#6e6259" />
        </Pressable>
      </View>
      {passwordError ? <Text style={styles.errorText}>{passwordError}</Text> : null}
      <TouchableOpacity style={styles.forgotPassword} onPress={() => router.replace('./forgot-password')}>
        <Text style={styles.forgotPasswordText}>Forgot Password?</Text>
      </TouchableOpacity>
      {/* Log In Button */}
      <TouchableOpacity 
        style={[styles.loginButton, isLoading && { opacity: 0.7 }]} 
        onPress={handleLogin}
        disabled={isLoading}
      >
        <Text style={styles.loginButtonText}>
          {isLoading ? 'Logging in...' : 'Log In'}
        </Text>
      </TouchableOpacity>
      {/* Divider */}
      <View style={styles.dividerContainer}>
        <View style={styles.divider} />
        <Text style={styles.orText}>or</Text>
        <View style={styles.divider} />
      </View>
      {/* Social Login Buttons */}
      <TouchableOpacity style={styles.socialButton}>
        <AntDesign name="google" size={22} color="#EA4335" style={styles.socialIcon} />
        <Text style={styles.socialButtonText}>Continue with Google</Text>
      </TouchableOpacity>
      <TouchableOpacity style={styles.socialButton}>
        <FontAwesome name="github" size={22} color="#000" style={styles.socialIcon} />
        <Text style={styles.socialButtonText}>Continue with GitHub</Text>
      </TouchableOpacity>
      <TouchableOpacity style={styles.socialButton}>
        <AntDesign name="apple1" size={22} color="#000" style={styles.socialIcon} />
        <Text style={styles.socialButtonText}>Continue with Apple</Text>
      </TouchableOpacity>
      {/* Sign Up Link */}
      <TouchableOpacity onPress={() => router.replace('./signup')}>
        <Text style={styles.signupLink}>Don't have an account? Sign Up</Text>
      </TouchableOpacity>
    </SafeAreaView>
  );
};

export default LoginScreen;

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    paddingHorizontal: 20,
    paddingTop: 30,
  },
  closeButton: {
    position: 'absolute',
    top: 18,
    right: 18,
    zIndex: 10,
    backgroundColor: '#f5f5f5',
    borderRadius: 20,
    padding: 4,
    elevation: 2,
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#FF6600',
    marginTop: 30, // reduced from 60
    marginBottom: 32,
    textAlign: 'center',
  },
  label: {
    fontSize: 15,
    color: '#3a2d1a',
    marginBottom: 6,
    marginLeft: 2,
    fontWeight: '500',
  },
  input: {
    width: '100%',
    height: 48,
    borderWidth: 1,
    borderColor: '#d6d6d6',
    borderRadius: 8,
    paddingHorizontal: 14,
    marginBottom: 0, // set to 0 for tight error placement
    fontSize: 16,
    color: '#3a2d1a',
    backgroundColor: '#fff',
  },
  passwordContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    width: '100%',
    marginBottom: 0,
  },
  eyeIcon: {
    position: 'absolute',
    right: 14,
    top: 13,
  },
  forgotPassword: {
    alignSelf: 'flex-end',
    marginBottom: 24,
  },
  forgotPasswordText: {
    color: '#FF6600',
    fontWeight: '500',
    fontSize: 14,
  },
  loginButton: {
    width: '100%',
    backgroundColor: '#FF6600',
    paddingVertical: 14,
    borderRadius: 8,
    alignItems: 'center',
    marginBottom: 24,
    marginTop: 4,
  },
  loginButtonText: {
    color: '#fff',
    fontSize: 18,
    fontWeight: 'bold',
  },
  dividerContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 18,
  },
  divider: {
    flex: 1,
    height: 1,
    backgroundColor: '#e0e0e0',
  },
  orText: {
    marginHorizontal: 10,
    color: '#888',
    fontWeight: '500',
  },
  socialButton: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#faf9f7',
    borderWidth: 1,
    borderColor: '#e0e0e0',
    borderRadius: 8,
    paddingVertical: 12,
    paddingHorizontal: 10,
    marginBottom: 14,
  },
  socialIcon: {
    marginRight: 12,
  },
  socialButtonText: {
    fontSize: 16,
    color: '#222',
    fontWeight: '500',
    textAlign: 'center',
  },
  signupLink: {
    marginTop: 18,
    color: '#FF6600',
    textAlign: 'center',
    fontWeight: '500',
    fontSize: 15,
  },
  inputError: {
    borderColor: '#FF3B30',
  },
  errorText: {
    color: '#FF3B30',
    fontSize: 13,
    marginTop: 0,
    marginBottom: 8,
    marginLeft: 2,
    fontWeight: '500',
    textAlign: 'left',
  },
});