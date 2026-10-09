import React, { useState, useEffect, useMemo } from 'react';
import {
  Shield,
  Wand2,
  Crosshair,
  Zap,
  Sword,
  Sparkles,
  Lock,
  User,
  LogOut,
  ChevronRight,
  Plus,
  Coins,
  Gem,
  Backpack,
  Activity,
  Play,
  RotateCcw,
  Gift,
  Check,
  X,
  Dice5,
  Dumbbell,
  Eye,
  AlertCircle,
  Calendar,
  Award,
  Trophy,
  Target,
  CheckCircle2,
  Skull,
  Swords
} from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { HeroClassType, HeroClassInfo, EquipmentItem, EquipmentType, RarityType, HeroCharacter, CalculatedStats, UserAccount, DailyMission, MissionCategory } from './types';
import { HERO_CLASSES, getStarterInventoryFor, getRandomLoot, getDefaultDailyMissions } from './data/constants';

export default function App() {
  // Navigation / View state
  const [currentUser, setCurrentUser] = useState<UserAccount | null>(() => {
    const saved = localStorage.getItem('rpg_current_user');
    return saved ? JSON.parse(saved) : null;
  });

  const [characters, setCharacters] = useState<HeroCharacter[]>(() => {
    const saved = localStorage.getItem('rpg_all_characters');
    return saved ? JSON.parse(saved) : [];
  });

  const [activeCharacterId, setActiveCharacterId] = useState<string | null>(() => {
    return currentUser?.activeCharacterId || null;
  });

  const [inventories, setInventories] = useState<Record<string, EquipmentItem[]>>(() => {
    const saved = localStorage.getItem('rpg_inventories');
    return saved ? JSON.parse(saved) : {};
  });

  // Current active character object
  const activeCharacter = useMemo(() => {
    return characters.find(c => c.id === activeCharacterId) || null;
  }, [characters, activeCharacterId]);

  // Current inventory
  const currentInventory = useMemo(() => {
    if (!activeCharacterId) return [];
    return inventories[activeCharacterId] || [];
  }, [inventories, activeCharacterId]);

  // Current equipped items
  const equippedItems = useMemo(() => {
    return currentInventory.filter(item => item.isEquipped);
  }, [currentInventory]);

  // Calculate character stats
  const calculatedStats = useMemo<CalculatedStats>(() => {
    if (!activeCharacter) {
      return {
        hp: 1000, mp: 500, physicalAtk: 100, magicAtk: 50, def: 100, critRate: 10, speed: 50,
        combatPower: 1500, bonusHp: 0, bonusMp: 0, bonusAtk: 0, bonusMatk: 0, bonusDef: 0, bonusCrit: 0, bonusSpeed: 0
      };
    }

    const cls = HERO_CLASSES[activeCharacter.heroClass];
    const levelMult = 1 + (activeCharacter.level - 1) * 0.12;

    let bonusHp = 0;
    let bonusMp = 0;
    let bonusAtk = 0;
    let bonusMatk = 0;
    let bonusDef = 0;
    let bonusCrit = 0;
    let bonusSpeed = 0;

    for (const item of equippedItems) {
      bonusHp += item.hpBonus;
      bonusMp += item.mpBonus;
      bonusAtk += item.atkBonus;
      bonusMatk += item.matkBonus;
      bonusDef += item.defBonus;
      bonusCrit += item.critBonus;
      bonusSpeed += item.speedBonus;
    }

    const hp = Math.floor(cls.baseHp * levelMult + bonusHp);
    const mp = Math.floor(cls.baseMp * levelMult + bonusMp);
    const physicalAtk = Math.floor(cls.basePhysicalAtk * levelMult + bonusAtk);
    const magicAtk = Math.floor(cls.baseMagicAtk * levelMult + bonusMatk);
    const def = Math.floor(cls.baseDef * levelMult + bonusDef);
    const critRate = Math.min(100, cls.baseCritRate + bonusCrit);
    const speed = Math.floor(cls.baseSpeed * (1 + (activeCharacter.level - 1) * 0.05) + bonusSpeed);

    const combatPower = Math.floor(
      hp * 0.25 + mp * 0.15 + physicalAtk * 1.8 + magicAtk * 1.8 + def * 1.4 + critRate * 12 + speed * 1.1
    );

    return {
      hp, mp, physicalAtk, magicAtk, def, critRate, speed, combatPower,
      bonusHp, bonusMp, bonusAtk, bonusMatk, bonusDef, bonusCrit, bonusSpeed
    };
  }, [activeCharacter, equippedItems]);

  // Screen selection state: 'AUTH' | 'CLASS_SELECT' | 'DASHBOARD'
  const [screen, setScreen] = useState<'AUTH' | 'CLASS_SELECT' | 'DASHBOARD'>(() => {
    if (!currentUser) return 'AUTH';
    if (!activeCharacterId) return 'CLASS_SELECT';
    return 'DASHBOARD';
  });

  // UI States
  const [authMode, setAuthMode] = useState<'LOGIN' | 'REGISTER'>('LOGIN');
  const [authUsername, setAuthUsername] = useState('');
  const [authPassword, setAuthPassword] = useState('');
  const [authConfirmPass, setAuthConfirmPass] = useState('');
  const [authError, setAuthError] = useState<string | null>(null);

  // Class selection state
  const [selectedClass, setSelectedClass] = useState<HeroClassType>('WARRIOR');
  const [showNicknameModal, setShowNicknameModal] = useState(false);
  const [nicknameInput, setNicknameInput] = useState('');
  const [nicknameError, setNicknameError] = useState<string | null>(null);

  // Dashboard Tab state
  const [activeTab, setActiveTab] = useState<'OVERVIEW' | 'INVENTORY' | 'MISSIONS' | 'DUNGEON' | 'ACCOUNT'>('OVERVIEW');
  const [selectedInventoryFilter, setSelectedInventoryFilter] = useState<EquipmentType | 'ALL'>('ALL');
  const [inspectedItem, setInspectedItem] = useState<EquipmentItem | null>(null);
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  // Daily Missions state
  const [dailyMissions, setDailyMissions] = useState<DailyMission[]>(() => {
    const saved = localStorage.getItem('rpg_daily_missions_' + (currentUser?.activeCharacterId || 'default'));
    return saved ? JSON.parse(saved) : getDefaultDailyMissions();
  });

  const [dailyChestClaimed, setDailyChestClaimed] = useState<boolean>(() => {
    const saved = localStorage.getItem('rpg_daily_chest_' + (currentUser?.activeCharacterId || 'default'));
    return saved ? JSON.parse(saved) : false;
  });

  // Calculate unclaimed completed missions
  const unclaimedMissionsCount = useMemo(() => {
    return dailyMissions.filter(m => m.currentCount >= m.targetCount && !m.isClaimed).length;
  }, [dailyMissions]);

  const completedMissionsCount = useMemo(() => {
    return dailyMissions.filter(m => m.currentCount >= m.targetCount).length;
  }, [dailyMissions]);

  // Dungeon battle state
  interface BattleLogItem {
    id: string;
    text: string;
    isCrit?: boolean;
    isPlayer?: boolean;
    isDamageToHero?: boolean;
    damage?: number;
    time: string;
  }

  const [isBattling, setIsBattling] = useState(false);
  const [battleLogs, setBattleLogs] = useState<BattleLogItem[]>([]);
  const [isHeroTakingDamage, setIsHeroTakingDamage] = useState(false);
  const [heroDamageTaken, setHeroDamageTaken] = useState<number | null>(null);
  const [isMonsterTakingDamage, setIsMonsterTakingDamage] = useState(false);
  const [monsterDamageTaken, setMonsterDamageTaken] = useState<{ amount: number; isCrit: boolean } | null>(null);
  const [activeMonster, setActiveMonster] = useState<{
    name: string;
    currentHp: number;
    maxHp: number;
    attack: number;
  } | null>(null);
  const [heroCurrentHp, setHeroCurrentHp] = useState<number>(() => calculatedStats.hp);

  // Keep hero HP in sync when not in battle
  useEffect(() => {
    if (!isBattling) {
      setHeroCurrentHp(calculatedStats.hp);
    }
  }, [calculatedStats.hp, isBattling]);

  // Toast auto dismiss
  useEffect(() => {
    if (toastMessage) {
      const timer = setTimeout(() => setToastMessage(null), 3500);
      return () => clearTimeout(timer);
    }
  }, [toastMessage]);

  // Save to localStorage
  useEffect(() => {
    if (currentUser) {
      localStorage.setItem('rpg_current_user', JSON.stringify(currentUser));
    } else {
      localStorage.removeItem('rpg_current_user');
    }
  }, [currentUser]);

  useEffect(() => {
    localStorage.setItem('rpg_all_characters', JSON.stringify(characters));
  }, [characters]);

  useEffect(() => {
    localStorage.setItem('rpg_inventories', JSON.stringify(inventories));
  }, [inventories]);

  // Sync daily missions per character
  useEffect(() => {
    if (activeCharacterId) {
      const saved = localStorage.getItem('rpg_daily_missions_' + activeCharacterId);
      setDailyMissions(saved ? JSON.parse(saved) : getDefaultDailyMissions());

      const savedChest = localStorage.getItem('rpg_daily_chest_' + activeCharacterId);
      setDailyChestClaimed(savedChest ? JSON.parse(savedChest) : false);
    }
  }, [activeCharacterId]);

  useEffect(() => {
    if (activeCharacterId) {
      localStorage.setItem('rpg_daily_missions_' + activeCharacterId, JSON.stringify(dailyMissions));
      localStorage.setItem('rpg_daily_chest_' + activeCharacterId, JSON.stringify(dailyChestClaimed));
    }
  }, [dailyMissions, dailyChestClaimed, activeCharacterId]);

  // Daily Mission helpers
  const updateMissionProgress = (category: MissionCategory, amount = 1) => {
    setDailyMissions(prev => {
      let newlyCompleted = false;
      let missionName = '';

      const updated = prev.map(m => {
        if (m.category === category && m.currentCount < m.targetCount) {
          const nextCount = Math.min(m.targetCount, m.currentCount + amount);
          if (nextCount >= m.targetCount && !m.isClaimed) {
            newlyCompleted = true;
            missionName = m.title;
          }
          return { ...m, currentCount: nextCount };
        }
        return m;
      });

      if (newlyCompleted) {
        setToastMessage(`Misi Selesai: "${missionName}"! Hadiah siap diklaim di tab Misi.`);
      }

      return updated;
    });
  };

  const handleClaimMission = (missionId: string) => {
    if (!activeCharacterId || !activeCharacter) return;
    const mission = dailyMissions.find(m => m.id === missionId);
    if (!mission || mission.isClaimed || mission.currentCount < mission.targetCount) return;

    let newExp = activeCharacter.currentExp + mission.expReward;
    let newLevel = activeCharacter.level;
    let newMaxExp = activeCharacter.maxExp;
    let didLevelUp = false;

    while (newExp >= newMaxExp) {
      newExp -= newMaxExp;
      newLevel += 1;
      newMaxExp = Math.floor(newMaxExp * 1.35);
      didLevelUp = true;
    }

    setCharacters(prev => prev.map(c => c.id === activeCharacterId ? {
      ...c,
      level: newLevel,
      currentExp: newExp,
      maxExp: newMaxExp,
      gold: c.gold + mission.goldReward,
      diamonds: c.diamonds + (mission.diamondReward || 0)
    } : c));

    setDailyMissions(prev => prev.map(m => m.id === missionId ? { ...m, isClaimed: true } : m));

    if (didLevelUp) {
      setToastMessage(`Hadiah Diklaim: +${mission.goldReward} Gold & +${mission.expReward} EXP! LEVEL UP ke Lv.${newLevel}!`);
    } else {
      setToastMessage(`Hadiah Diklaim: +${mission.goldReward} Gold & +${mission.expReward} EXP!`);
    }
  };

  const handleClaimDailyChest = () => {
    if (!activeCharacterId || !activeCharacter || dailyChestClaimed) return;
    const allDone = dailyMissions.every(m => m.currentCount >= m.targetCount);
    if (!allDone) {
      setToastMessage('Selesaikan semua 5 misi harian terlebih dahulu!');
      return;
    }

    const bonusGold = 600;
    const bonusExp = 250;
    const bonusDiamonds = 15;

    let newExp = activeCharacter.currentExp + bonusExp;
    let newLevel = activeCharacter.level;
    let newMaxExp = activeCharacter.maxExp;
    let didLevelUp = false;

    while (newExp >= newMaxExp) {
      newExp -= newMaxExp;
      newLevel += 1;
      newMaxExp = Math.floor(newMaxExp * 1.35);
      didLevelUp = true;
    }

    setCharacters(prev => prev.map(c => c.id === activeCharacterId ? {
      ...c,
      level: newLevel,
      currentExp: newExp,
      maxExp: newMaxExp,
      gold: c.gold + bonusGold,
      diamonds: c.diamonds + bonusDiamonds
    } : c));

    setDailyChestClaimed(true);
    setToastMessage(`PETI HARIAN DIBUKA! +${bonusGold} Gold, +${bonusExp} EXP, +${bonusDiamonds} Ruby!`);
  };

  const handleResetDailyMissions = () => {
    setDailyMissions(getDefaultDailyMissions());
    setDailyChestClaimed(false);
    setToastMessage('Misi harian telah di-reset untuk hari ini!');
  };

  // Handlers
  const handleAuthSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setAuthError(null);

    const userClean = authUsername.trim();
    if (!userClean || !authPassword) {
      setAuthError('Harap isi semua kolom formulir.');
      return;
    }

    if (authMode === 'REGISTER') {
      if (authPassword !== authConfirmPass) {
        setAuthError('Konfirmasi kata sandi tidak cocok.');
        return;
      }
      const existingUsers: UserAccount[] = JSON.parse(localStorage.getItem('rpg_users_list') || '[]');
      if (existingUsers.some(u => u.username.toLowerCase() === userClean.toLowerCase())) {
        setAuthError('Username ini sudah terdaftar. Silakan login.');
        return;
      }

      const newUser: UserAccount = {
        username: userClean,
        passwordHash: authPassword,
        activeCharacterId: null
      };
      localStorage.setItem('rpg_users_list', JSON.stringify([...existingUsers, newUser]));
      setCurrentUser(newUser);
      setActiveCharacterId(null);
      setScreen('CLASS_SELECT');
      setToastMessage(`Akun ${userClean} berhasil didaftarkan! Silakan pilih class karaktermu.`);
    } else {
      // Login
      const existingUsers: UserAccount[] = JSON.parse(localStorage.getItem('rpg_users_list') || '[]');
      const found = existingUsers.find(
        u => u.username.toLowerCase() === userClean.toLowerCase() && u.passwordHash === authPassword
      );

      if (found) {
        setCurrentUser(found);
        if (found.activeCharacterId && characters.some(c => c.id === found.activeCharacterId)) {
          setActiveCharacterId(found.activeCharacterId);
          setScreen('DASHBOARD');
          setToastMessage(`Selamat datang kembali, ${found.username}!`);
        } else {
          // Check if user has characters
          const userChars = characters.filter(c => c.userId === found.username);
          if (userChars.length > 0) {
            setActiveCharacterId(userChars[0].id);
            setScreen('DASHBOARD');
          } else {
            setActiveCharacterId(null);
            setScreen('CLASS_SELECT');
          }
        }
      } else {
        setAuthError('Username atau kata sandi salah. Atau klik Main Cepat!');
      }
    }
  };

  const handleQuickDemo = () => {
    const demoUser = 'PahlawanEldoria';
    const demoPass = 'rpg123';
    const existingUsers: UserAccount[] = JSON.parse(localStorage.getItem('rpg_users_list') || '[]');
    let user = existingUsers.find(u => u.username === demoUser);

    if (!user) {
      user = { username: demoUser, passwordHash: demoPass, activeCharacterId: null };
      localStorage.setItem('rpg_users_list', JSON.stringify([...existingUsers, user]));
    }

    setCurrentUser(user);
    const userChars = characters.filter(c => c.userId === demoUser);
    if (userChars.length > 0) {
      setActiveCharacterId(userChars[0].id);
      setScreen('DASHBOARD');
    } else {
      setActiveCharacterId(null);
      setScreen('CLASS_SELECT');
    }
    setToastMessage('Berhasil masuk dengan Akun Demo!');
  };

  const handleRandomizeName = () => {
    const namesByClass = {
      WARRIOR: ['Ares', 'Valerius', 'Galahad', 'Leonidas', 'Bartholomew', 'IronClad'],
      MAGE: ['Morpheus', 'Arcanist', 'Ignis', 'Zephyr', 'Astraea', 'Solomon'],
      ARCHER: ['Hawkeye', 'Sylvana', 'Fletcher', 'WindStrider', 'HunterX', 'Robin'],
      ASSASSIN: ['ShadowFang', 'Nyx', 'BloodRaven', 'SilentBlade', 'Kaelen', 'Phantom']
    };
    const pool = namesByClass[selectedClass];
    const random = pool[Math.floor(Math.random() * pool.length)];
    setNicknameInput(random);
    setNicknameError(null);
  };

  const handleConfirmCharacter = () => {
    const nick = nicknameInput.trim();
    if (!nick || nick.length < 3 || nick.length > 16) {
      setNicknameError('Nickname harus terdiri dari 3 hingga 16 karakter.');
      return;
    }

    if (!currentUser) return;

    const newCharId = 'char_' + Math.random().toString(36).substring(2, 9);
    const newCharacter: HeroCharacter = {
      id: newCharId,
      userId: currentUser.username,
      nickname: nick,
      heroClass: selectedClass,
      level: 1,
      currentExp: 0,
      maxExp: 100,
      gold: 1500,
      diamonds: 50,
      createdAt: Date.now()
    };

    // Starter equipment
    const starterGear = getStarterInventoryFor(selectedClass);

    setCharacters(prev => [...prev, newCharacter]);
    setInventories(prev => ({ ...prev, [newCharId]: starterGear }));
    setActiveCharacterId(newCharId);

    const updatedUser = { ...currentUser, activeCharacterId: newCharId };
    setCurrentUser(updatedUser);

    setShowNicknameModal(false);
    setScreen('DASHBOARD');
    setToastMessage(`Karakter ${nick} (${HERO_CLASSES[selectedClass].displayName}) berhasil dibuat!`);
  };

  const handleEquipItem = (item: EquipmentItem) => {
    if (!activeCharacterId || !activeCharacter) return;
    if (item.requiredClass && item.requiredClass !== activeCharacter.heroClass) {
      setToastMessage(`Equipment ini khusus untuk kelas ${HERO_CLASSES[item.requiredClass].displayName}!`);
      return;
    }

    setInventories(prev => {
      const items = prev[activeCharacterId] || [];
      const updated = items.map(it => {
        if (it.id === item.id) {
          return { ...it, isEquipped: true };
        }
        if (it.type === item.type && it.isEquipped) {
          return { ...it, isEquipped: false };
        }
        return it;
      });
      return { ...prev, [activeCharacterId]: updated };
    });

    setInspectedItem(prev => prev ? { ...prev, isEquipped: true } : null);
    setToastMessage(`${item.name} berhasil dipasang!`);
    updateMissionProgress('EQUIP', 1);
  };

  const handleUnequipItem = (item: EquipmentItem) => {
    if (!activeCharacterId) return;

    setInventories(prev => {
      const items = prev[activeCharacterId] || [];
      const updated = items.map(it => it.id === item.id ? { ...it, isEquipped: false } : it);
      return { ...prev, [activeCharacterId]: updated };
    });

    setInspectedItem(prev => prev ? { ...prev, isEquipped: false } : null);
    setToastMessage(`${item.name} berhasil dilepas!`);
  };

  const handleSellItem = (item: EquipmentItem) => {
    if (!activeCharacterId || !activeCharacter) return;
    if (item.isEquipped) {
      setToastMessage('Lepas equipment terlebih dahulu sebelum menjual!');
      return;
    }

    const price = item.rarity === 'LEGENDARY' ? 3000 : item.rarity === 'EPIC' ? 1200 : item.rarity === 'RARE' ? 450 : 150;

    setInventories(prev => {
      const items = prev[activeCharacterId] || [];
      return { ...prev, [activeCharacterId]: items.filter(it => it.id !== item.id) };
    });

    setCharacters(prev => prev.map(c => c.id === activeCharacterId ? { ...c, gold: c.gold + price } : c));
    setInspectedItem(null);
    setToastMessage(`Berhasil menjual ${item.name} seharga +${price} Gold!`);
  };

  const handleOpenLootChest = () => {
    if (!activeCharacterId || !activeCharacter) return;
    const cost = 250;
    if (activeCharacter.gold < cost) {
      setToastMessage(`Gold tidak mencukupi! Butuh ${cost} Gold untuk membuka peti.`);
      return;
    }

    const newItem = getRandomLoot(activeCharacter.heroClass);
    setInventories(prev => ({
      ...prev,
      [activeCharacterId]: [...(prev[activeCharacterId] || []), newItem]
    }));

    setCharacters(prev => prev.map(c => c.id === activeCharacterId ? { ...c, gold: c.gold - cost } : c));
    setInspectedItem(newItem);
    setToastMessage(`Peti Harta dibuka! Mendapatkan: [${newItem.rarity}] ${newItem.name}!`);
    updateMissionProgress('CHEST', 1);
  };

  const handleTrainCharacter = () => {
    if (!activeCharacterId || !activeCharacter) return;
    const expGain = 45;
    const goldGain = 80;

    let newExp = activeCharacter.currentExp + expGain;
    let newLevel = activeCharacter.level;
    let newMaxExp = activeCharacter.maxExp;

    let didLevelUp = false;
    while (newExp >= newMaxExp) {
      newExp -= newMaxExp;
      newLevel += 1;
      newMaxExp = Math.floor(newMaxExp * 1.35);
      didLevelUp = true;
    }

    setCharacters(prev => prev.map(c => c.id === activeCharacterId ? {
      ...c,
      level: newLevel,
      currentExp: newExp,
      maxExp: newMaxExp,
      gold: c.gold + goldGain
    } : c));

    updateMissionProgress('TRAIN', 1);

    if (didLevelUp) {
      setToastMessage(`LEVEL UP! Sekarang Level ${newLevel}! Semua statistik meningkat!`);
    } else {
      setToastMessage(`Latihan selesai! +${expGain} EXP & +${goldGain} Gold diperoleh.`);
    }
  };

  const getNowTime = () => new Date().toLocaleTimeString('id-ID', { hour: '2-digit', minute: '2-digit', second: '2-digit' });

  const handleStartDungeon = () => {
    if (!activeCharacter || isBattling) return;
    setIsBattling(true);

    const monsters = [
      { name: 'Gorgon Batu Berbisa', maxHp: Math.floor(calculatedStats.hp * 0.85), attack: 85 },
      { name: 'Serigala Gua Neraka', maxHp: Math.floor(calculatedStats.hp * 0.75), attack: 95 },
      { name: 'Ksatria Kerangka Kegelapan', maxHp: Math.floor(calculatedStats.hp * 0.9), attack: 80 },
      { name: 'Naga Api Kerdil', maxHp: Math.floor(calculatedStats.hp * 1.05), attack: 110 }
    ];
    const pickedMonster = monsters[Math.floor(Math.random() * monsters.length)];

    setActiveMonster({
      name: pickedMonster.name,
      currentHp: pickedMonster.maxHp,
      maxHp: pickedMonster.maxHp,
      attack: pickedMonster.attack
    });
    setHeroCurrentHp(calculatedStats.hp);
    setIsHeroTakingDamage(false);
    setHeroDamageTaken(null);
    setIsMonsterTakingDamage(false);
    setMonsterDamageTaken(null);

    setBattleLogs([
      {
        id: 'log_enter_' + Date.now(),
        text: `⚔️ ${activeCharacter.nickname} memasuki Ruang Bawah Tanah Kuno...`,
        isPlayer: true,
        time: getNowTime()
      }
    ]);

    setTimeout(() => {
      setBattleLogs(prev => [
        ...prev,
        {
          id: 'log_spawn_' + Date.now(),
          text: `⚠️ Monster muncul: ${pickedMonster.name} (HP ${pickedMonster.maxHp})! Siap bertempur!`,
          time: getNowTime()
        }
      ]);

      setTimeout(() => {
        // Player attack calculation
        const isCrit = Math.random() * 100 <= calculatedStats.critRate;
        const critMult = isCrit ? 2.2 : 1.0;
        let dmg = 0;

        if (activeCharacter.heroClass === 'WARRIOR') {
          dmg = Math.floor(calculatedStats.physicalAtk * 1.3 * critMult);
        } else if (activeCharacter.heroClass === 'MAGE') {
          dmg = Math.floor(calculatedStats.magicAtk * 1.6 * critMult);
        } else if (activeCharacter.heroClass === 'ARCHER') {
          dmg = Math.floor(calculatedStats.physicalAtk * 1.7 * critMult);
        } else {
          // ASSASSIN
          dmg = Math.floor(calculatedStats.physicalAtk * 1.45 * (isCrit ? 2.5 : 1.0));
        }

        // Trigger monster hit animation
        setIsMonsterTakingDamage(true);
        setMonsterDamageTaken({ amount: dmg, isCrit });
        setActiveMonster(prev => prev ? { ...prev, currentHp: Math.max(0, prev.currentHp - dmg) } : null);

        setTimeout(() => {
          setIsMonsterTakingDamage(false);
          setMonsterDamageTaken(null);
        }, 500);

        const critText = isCrit ? '💥 [CRITICAL HIT MEMATIKAN!]' : '⚔️';
        setBattleLogs(prev => [
          ...prev,
          {
            id: 'log_atk_' + Date.now(),
            text: `${critText} ${activeCharacter.nickname} menyerang! Menimbulkan ${dmg} Damage!`,
            isCrit,
            isPlayer: true,
            damage: dmg,
            time: getNowTime()
          }
        ]);

        setTimeout(() => {
          // Monster counter attack
          const reduction = activeCharacter.heroClass === 'WARRIOR' ? 0.75 : 1.0;
          const monsterDmg = Math.floor(Math.max(20, (120 - calculatedStats.def * 0.3) * reduction));
          const warriorNote = activeCharacter.heroClass === 'WARRIOR' ? ' (Pasif Iron Wall menyerap 25% damage!)' : '';

          // Trigger subtle shake effect on Hero when taking damage
          setIsHeroTakingDamage(true);
          setHeroDamageTaken(monsterDmg);
          setHeroCurrentHp(prev => Math.max(1, prev - monsterDmg));

          setTimeout(() => {
            setIsHeroTakingDamage(false);
            setHeroDamageTaken(null);
          }, 550);

          setBattleLogs(prev => [
            ...prev,
            {
              id: 'log_counter_' + Date.now(),
              text: `🛡️ ${pickedMonster.name} membalas! ${activeCharacter.nickname} menerima ${monsterDmg} Damage${warriorNote}.`,
              isDamageToHero: true,
              damage: monsterDmg,
              time: getNowTime()
            }
          ]);

          setTimeout(() => {
            // Victory & Finishing blow
            setActiveMonster(prev => prev ? { ...prev, currentHp: 0 } : null);
            const goldReward = Math.floor(150 + Math.random() * 120);
            const expReward = Math.floor(70 + Math.random() * 50);

            setBattleLogs(prev => [
              ...prev,
              {
                id: 'log_finish_' + Date.now(),
                text: `✨ Serangan pamungkas merobohkan ${pickedMonster.name}!`,
                isCrit: true,
                time: getNowTime()
              },
              {
                id: 'log_victory_' + (Date.now() + 1),
                text: `🏆 Kemenangan! Hadiah: +${goldReward} Gold & +${expReward} EXP!`,
                time: getNowTime()
              }
            ]);

            // Add exp & gold
            let newExp = activeCharacter.currentExp + expReward;
            let newLevel = activeCharacter.level;
            let newMaxExp = activeCharacter.maxExp;
            while (newExp >= newMaxExp) {
              newExp -= newMaxExp;
              newLevel += 1;
              newMaxExp = Math.floor(newMaxExp * 1.35);
            }

            setCharacters(prev => prev.map(c => c.id === activeCharacterId ? {
              ...c,
              level: newLevel,
              currentExp: newExp,
              maxExp: newMaxExp,
              gold: c.gold + goldReward
            } : c));

            setIsBattling(false);
            setToastMessage(`Dungeon Selesai! +${goldReward} Gold & +${expReward} EXP!`);
            updateMissionProgress('DUNGEON', 1);
          }, 700);
        }, 700);
      }, 700);
    }, 600);
  };

  const handleLogout = () => {
    setCurrentUser(null);
    setActiveCharacterId(null);
    setScreen('AUTH');
    setToastMessage('Berhasil keluar dari game.');
  };

  const getRarityBadgeColor = (rarity: RarityType) => {
    switch (rarity) {
      case 'COMMON': return 'text-slate-400 bg-slate-400/10 border-slate-400/30';
      case 'RARE': return 'text-blue-400 bg-blue-400/10 border-blue-400/40';
      case 'EPIC': return 'text-purple-400 bg-purple-400/10 border-purple-400/50';
      case 'LEGENDARY': return 'text-amber-400 bg-amber-400/15 border-amber-400/60 shadow-[0_0_10px_rgba(251,191,36,0.2)]';
    }
  };

  const renderClassIcon = (heroClass: HeroClassType, size = 'w-5 h-5') => {
    switch (heroClass) {
      case 'WARRIOR': return <Shield className={`${size} text-red-500`} />;
      case 'MAGE': return <Wand2 className={`${size} text-purple-400`} />;
      case 'ARCHER': return <Crosshair className={`${size} text-emerald-400`} />;
      case 'ASSASSIN': return <Zap className={`${size} text-rose-500`} />;
    }
  };

  return (
    <div className="min-h-screen bg-[#0D1117] text-[#F0F6FC] flex flex-col font-sans select-none">
      {/* Toast Notification */}
      {toastMessage && (
        <div className="fixed top-4 left-1/2 -translate-x-1/2 z-50 bg-[#161B22] border border-[#FFB300]/50 text-[#FFD54F] px-5 py-2.5 rounded-xl shadow-2xl flex items-center gap-2 text-sm font-semibold animate-bounce">
          <Sparkles className="w-4 h-4 text-[#FFB300]" />
          <span>{toastMessage}</span>
        </div>
      )}

      {/* VIEW 1: AUTH SCREEN (LOGIN / REGISTER) */}
      {screen === 'AUTH' && (
        <div className="flex-1 flex flex-col items-center justify-center p-4 sm:p-6 bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-[#1C2333] via-[#0D1117] to-[#0A0D12]">
          <div className="w-full max-w-md">
            {/* Game Brand Emblem */}
            <div className="text-center mb-8">
              <div className="w-20 h-20 mx-auto mb-4 rounded-2xl bg-gradient-to-tr from-[#212836] to-[#161B22] border-2 border-[#FFB300] flex items-center justify-center shadow-[0_0_30px_rgba(255,179,0,0.25)]">
                <Shield className="w-10 h-10 text-[#FFB300]" />
              </div>
              <h1 className="rpg-font text-3xl sm:text-4xl font-black text-transparent bg-clip-text bg-gradient-to-r from-[#FFD54F] via-[#FFB300] to-[#FFA000] tracking-wider">
                VALIANT ORIGIN
              </h1>
              <p className="text-xs uppercase tracking-[0.25em] text-slate-400 mt-1 font-semibold">
                Chronicles of Eldoria Realm
              </p>
            </div>

            {/* Auth Card */}
            <div className="bg-[#161B22] border border-[#30363D] rounded-2xl p-6 shadow-2xl backdrop-blur-sm">
              {/* Tabs */}
              <div className="grid grid-cols-2 p-1 bg-[#212836] rounded-xl mb-6">
                <button
                  type="button"
                  onClick={() => { setAuthMode('LOGIN'); setAuthError(null); }}
                  className={`py-2 text-sm font-bold rounded-lg transition-all ${
                    authMode === 'LOGIN' ? 'bg-[#FFB300] text-[#0D1117] shadow' : 'text-slate-400 hover:text-white'
                  }`}
                >
                  Masuk (Login)
                </button>
                <button
                  type="button"
                  onClick={() => { setAuthMode('REGISTER'); setAuthError(null); }}
                  className={`py-2 text-sm font-bold rounded-lg transition-all ${
                    authMode === 'REGISTER' ? 'bg-[#FFB300] text-[#0D1117] shadow' : 'text-slate-400 hover:text-white'
                  }`}
                >
                  Daftar Akun
                </button>
              </div>

              {authError && (
                <div className="mb-4 p-3 bg-red-500/15 border border-red-500/40 rounded-xl text-red-400 text-xs flex items-center gap-2">
                  <AlertCircle className="w-4 h-4 flex-shrink-0" />
                  <span>{authError}</span>
                </div>
              )}

              <form onSubmit={handleAuthSubmit} className="space-y-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1">
                    Nama Pengguna (Username)
                  </label>
                  <div className="relative">
                    <User className="w-4 h-4 text-[#FFB300] absolute left-3.5 top-3.5" />
                    <input
                      type="text"
                      value={authUsername}
                      onChange={e => { setAuthUsername(e.target.value); setAuthError(null); }}
                      placeholder="Masukkan username..."
                      className="w-full bg-[#0D1117] border border-[#30363D] rounded-xl pl-10 pr-4 py-2.5 text-sm focus:outline-none focus:border-[#FFB300] transition"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1">
                    Kata Sandi (Password)
                  </label>
                  <div className="relative">
                    <Lock className="w-4 h-4 text-[#FFB300] absolute left-3.5 top-3.5" />
                    <input
                      type="password"
                      value={authPassword}
                      onChange={e => { setAuthPassword(e.target.value); setAuthError(null); }}
                      placeholder="Masukkan kata sandi..."
                      className="w-full bg-[#0D1117] border border-[#30363D] rounded-xl pl-10 pr-4 py-2.5 text-sm focus:outline-none focus:border-[#FFB300] transition"
                    />
                  </div>
                </div>

                {authMode === 'REGISTER' && (
                  <div>
                    <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1">
                      Ulangi Kata Sandi
                    </label>
                    <div className="relative">
                      <Lock className="w-4 h-4 text-[#FFB300] absolute left-3.5 top-3.5" />
                      <input
                        type="password"
                        value={authConfirmPass}
                        onChange={e => { setAuthConfirmPass(e.target.value); setAuthError(null); }}
                        placeholder="Ulangi kata sandi..."
                        className="w-full bg-[#0D1117] border border-[#30363D] rounded-xl pl-10 pr-4 py-2.5 text-sm focus:outline-none focus:border-[#FFB300] transition"
                      />
                    </div>
                  </div>
                )}

                <button
                  type="submit"
                  className="w-full py-3 bg-[#FFB300] hover:bg-[#FFA000] text-[#0D1117] font-black rounded-xl text-sm tracking-wide transition shadow-lg shadow-[#FFB300]/20 flex items-center justify-center gap-2 mt-2"
                >
                  <Play className="w-4 h-4 fill-current" />
                  <span>{authMode === 'REGISTER' ? 'DAFTAR & BUAT AKUN' : 'MASUK KE DUNIA GAME'}</span>
                </button>

                <div className="relative flex py-2 items-center">
                  <div className="flex-grow border-t border-[#30363D]"></div>
                  <span className="flex-shrink mx-3 text-xs text-slate-500 uppercase font-bold">atau</span>
                  <div className="flex-grow border-t border-[#30363D]"></div>
                </div>

                <button
                  type="button"
                  onClick={handleQuickDemo}
                  className="w-full py-2.5 bg-transparent hover:bg-[#212836] border border-[#FFB300]/40 text-[#FFD54F] font-bold rounded-xl text-sm transition flex items-center justify-center gap-2"
                >
                  <Sparkles className="w-4 h-4 text-[#FFB300]" />
                  <span>Main Cepat (Akun Demo Instan)</span>
                </button>
              </form>
            </div>

            <p className="text-center text-xs text-slate-500 mt-6 font-medium">
              4 Class Menantimu: Warrior • Mage • Archer • Assassin
            </p>
          </div>
        </div>
      )}

      {/* VIEW 2: CLASS SELECTION SCREEN */}
      {screen === 'CLASS_SELECT' && (
        <div className="flex-1 overflow-y-auto p-4 sm:p-6 max-w-4xl mx-auto w-full">
          {/* Header */}
          <div className="text-center mb-8 pt-4">
            <span className="inline-block px-3 py-1 rounded-full bg-[#FFB300]/10 border border-[#FFB300]/30 text-[#FFD54F] text-xs font-bold uppercase tracking-wider mb-2">
              Langkah 1 dari 2
            </span>
            <h2 className="rpg-font text-2xl sm:text-3xl font-black text-[#F0F6FC]">
              PILIH TAKDIR KELAS KARAKTER
            </h2>
            <p className="text-slate-400 text-sm max-w-lg mx-auto mt-1">
              Setiap class memiliki kelebihan, pasif tersendiri, dan status awal yang menyesuaikan gaya bertarung masing-masing.
            </p>
          </div>

          {/* 4 Class Cards Carousel / Grid */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 sm:gap-4 mb-6">
            {(Object.keys(HERO_CLASSES) as HeroClassType[]).map(cKey => {
              const cInfo = HERO_CLASSES[cKey];
              const isSelected = selectedClass === cKey;
              return (
                <button
                  key={cKey}
                  type="button"
                  onClick={() => setSelectedClass(cKey)}
                  className={`p-4 rounded-2xl border text-left transition-all relative overflow-hidden flex flex-col justify-between ${
                    isSelected
                      ? 'bg-[#212836] border-[#FFB300] shadow-[0_0_20px_rgba(255,179,0,0.2)] scale-[1.02]'
                      : 'bg-[#161B22] border-[#30363D] hover:border-slate-500 hover:bg-[#1C2333]'
                  }`}
                >
                  {isSelected && (
                    <div className="absolute top-2 right-2 w-2 h-2 rounded-full bg-[#FFB300] animate-ping" />
                  )}
                  <div>
                    <div className="w-12 h-12 rounded-xl flex items-center justify-center mb-3 bg-[#0D1117] border border-[#30363D]">
                      {renderClassIcon(cKey, 'w-6 h-6')}
                    </div>
                    <h3 className="font-bold text-base text-white">{cInfo.displayName}</h3>
                    <p className="text-[11px] font-semibold text-[#FFD54F] mt-0.5">{cInfo.mainAdvantage}</p>
                  </div>
                  <span className="text-[10px] text-slate-400 mt-3 font-medium block">
                    {cInfo.role}
                  </span>
                </button>
              );
            })}
          </div>

          {/* Active Class Showcase Detail Card */}
          {(() => {
            const activeInfo = HERO_CLASSES[selectedClass];
            const starterPreview = getStarterInventoryFor(selectedClass).filter(i => i.isEquipped);

            return (
              <div className="bg-[#161B22] border-2 border-[#FFB300]/60 rounded-3xl p-6 sm:p-8 shadow-2xl relative overflow-hidden mb-8">
                {/* Background glow accent */}
                <div
                  className="absolute -right-20 -top-20 w-64 h-64 rounded-full blur-3xl opacity-20 pointer-events-none"
                  style={{ backgroundColor: activeInfo.glowHex }}
                />

                {/* Top Class Banner */}
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-6 border-b border-[#30363D]">
                  <div className="flex items-center gap-4">
                    <div
                      className="w-16 h-16 rounded-2xl flex items-center justify-center border-2 shadow-lg"
                      style={{
                        backgroundColor: `${activeInfo.colorHex}25`,
                        borderColor: activeInfo.glowHex
                      }}
                    >
                      {renderClassIcon(selectedClass, 'w-8 h-8')}
                    </div>
                    <div>
                      <div className="flex items-center gap-2">
                        <h3 className="text-2xl font-black text-white">{activeInfo.displayName}</h3>
                        <span
                          className="px-2.5 py-0.5 text-xs font-bold rounded-md border"
                          style={{
                            color: activeInfo.glowHex,
                            borderColor: `${activeInfo.glowHex}60`,
                            backgroundColor: `${activeInfo.colorHex}20`
                          }}
                        >
                          {activeInfo.title}
                        </span>
                      </div>
                      <p className="text-xs text-slate-400 mt-1 font-medium">{activeInfo.role}</p>
                    </div>
                  </div>

                  <div className="bg-[#212836] border border-[#30363D] px-4 py-2.5 rounded-xl">
                    <span className="text-[10px] uppercase font-bold text-slate-400 tracking-wider block">Kelebihan Utama</span>
                    <span className="text-base font-extrabold text-[#FFB300]">{activeInfo.mainAdvantage}</span>
                  </div>
                </div>

                {/* Playstyle & Passive Skill Showcase */}
                <div className="grid sm:grid-cols-2 gap-4 my-6">
                  {/* Passive Skill */}
                  <div className="bg-[#212836] border border-[#30363D] p-4 rounded-xl">
                    <div className="flex items-center gap-2 text-[#FFB300] font-bold text-sm mb-1.5">
                      <Zap className="w-4 h-4 fill-current" />
                      <span>Pasif: {activeInfo.passiveName}</span>
                    </div>
                    <p className="text-xs text-slate-300 leading-relaxed font-medium">
                      {activeInfo.passiveDescription}
                    </p>
                  </div>

                  {/* Playstyle */}
                  <div className="bg-[#212836] border border-[#30363D] p-4 rounded-xl">
                    <div className="flex items-center gap-2 text-slate-200 font-bold text-sm mb-1.5">
                      <Eye className="w-4 h-4 text-[#FFB300]" />
                      <span>Gaya Bermain (Playstyle)</span>
                    </div>
                    <p className="text-xs text-slate-300 leading-relaxed font-medium">
                      {activeInfo.playstyleDescription}
                    </p>
                  </div>
                </div>

                {/* Tampilan Status Awal Karakter yang Menyesuaikan Gaya Bermain */}
                <div className="mb-6">
                  <h4 className="text-xs font-extrabold uppercase tracking-widest text-[#FFB300] mb-3">
                    STATUS AWAL KARAKTER
                  </h4>

                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 bg-[#0D1117] p-4 rounded-2xl border border-[#30363D]">
                    {/* HP */}
                    <div>
                      <div className="flex justify-between text-xs mb-1">
                        <span className="text-slate-400 font-semibold">Health Points (HP)</span>
                        <span className="font-bold text-red-400">{activeInfo.baseHp}</span>
                      </div>
                      <div className="h-2 bg-[#212836] rounded-full overflow-hidden">
                        <div
                          className="h-full bg-gradient-to-r from-red-600 to-red-400 rounded-full transition-all duration-500"
                          style={{ width: `${(activeInfo.baseHp / 1500) * 100}%` }}
                        />
                      </div>
                    </div>

                    {/* MP */}
                    <div>
                      <div className="flex justify-between text-xs mb-1">
                        <span className="text-slate-400 font-semibold">Mana Points (MP)</span>
                        <span className="font-bold text-blue-400">{activeInfo.baseMp}</span>
                      </div>
                      <div className="h-2 bg-[#212836] rounded-full overflow-hidden">
                        <div
                          className="h-full bg-gradient-to-r from-blue-600 to-blue-400 rounded-full transition-all duration-500"
                          style={{ width: `${(activeInfo.baseMp / 1300) * 100}%` }}
                        />
                      </div>
                    </div>

                    {/* Physical ATK */}
                    <div>
                      <div className="flex justify-between text-xs mb-1">
                        <span className="text-slate-400 font-semibold">Serangan Fisik (Physical ATK)</span>
                        <span className="font-bold text-amber-400">{activeInfo.basePhysicalAtk}</span>
                      </div>
                      <div className="h-2 bg-[#212836] rounded-full overflow-hidden">
                        <div
                          className="h-full bg-gradient-to-r from-amber-600 to-amber-400 rounded-full transition-all duration-500"
                          style={{ width: `${(activeInfo.basePhysicalAtk / 180) * 100}%` }}
                        />
                      </div>
                    </div>

                    {/* Magic ATK */}
                    <div>
                      <div className="flex justify-between text-xs mb-1">
                        <span className="text-slate-400 font-semibold">Serangan Sihir (Magic ATK)</span>
                        <span className="font-bold text-purple-400">{activeInfo.baseMagicAtk}</span>
                      </div>
                      <div className="h-2 bg-[#212836] rounded-full overflow-hidden">
                        <div
                          className="h-full bg-gradient-to-r from-purple-600 to-purple-400 rounded-full transition-all duration-500"
                          style={{ width: `${(activeInfo.baseMagicAtk / 180) * 100}%` }}
                        />
                      </div>
                    </div>

                    {/* DEF */}
                    <div>
                      <div className="flex justify-between text-xs mb-1">
                        <span className="text-slate-400 font-semibold">Pertahanan (DEF)</span>
                        <span className="font-bold text-teal-400">{activeInfo.baseDef}</span>
                      </div>
                      <div className="h-2 bg-[#212836] rounded-full overflow-hidden">
                        <div
                          className="h-full bg-gradient-to-r from-teal-600 to-teal-400 rounded-full transition-all duration-500"
                          style={{ width: `${(activeInfo.baseDef / 150) * 100}%` }}
                        />
                      </div>
                    </div>

                    {/* Crit Rate */}
                    <div>
                      <div className="flex justify-between text-xs mb-1">
                        <span className="text-slate-400 font-semibold">Peluang Kritis (Crit Rate)</span>
                        <span className="font-bold text-rose-400">{activeInfo.baseCritRate}%</span>
                      </div>
                      <div className="h-2 bg-[#212836] rounded-full overflow-hidden">
                        <div
                          className="h-full bg-gradient-to-r from-rose-600 to-rose-400 rounded-full transition-all duration-500"
                          style={{ width: `${(activeInfo.baseCritRate / 50) * 100}%` }}
                        />
                      </div>
                    </div>

                    {/* Speed */}
                    <div className="sm:col-span-2">
                      <div className="flex justify-between text-xs mb-1">
                        <span className="text-slate-400 font-semibold">Kecepatan Gerak (Speed / AGI)</span>
                        <span className="font-bold text-yellow-400">{activeInfo.baseSpeed}</span>
                      </div>
                      <div className="h-2 bg-[#212836] rounded-full overflow-hidden">
                        <div
                          className="h-full bg-gradient-to-r from-yellow-600 to-yellow-400 rounded-full transition-all duration-500"
                          style={{ width: `${(activeInfo.baseSpeed / 120) * 100}%` }}
                        />
                      </div>
                    </div>
                  </div>
                </div>

                {/* Preview Starter Gear */}
                <div className="mb-8">
                  <h4 className="text-xs font-extrabold uppercase tracking-widest text-[#FFB300] mb-2">
                    PERALATAN AWAL DI TAS INVENTARIS
                  </h4>
                  <div className="grid grid-cols-2 sm:grid-cols-3 gap-2">
                    {starterPreview.map(item => (
                      <div
                        key={item.id}
                        className="p-2.5 rounded-xl bg-[#212836] border border-[#30363D] flex items-center gap-2.5 text-xs"
                      >
                        <div className="w-8 h-8 rounded-lg bg-[#0D1117] flex items-center justify-center text-[#FFB300] flex-shrink-0">
                          <Sword className="w-4 h-4" />
                        </div>
                        <div className="min-w-0">
                          <div className="font-bold text-slate-200 truncate">{item.name}</div>
                          <div className="text-[10px] text-slate-400">{item.type}</div>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>

                {/* Primary Action Button */}
                <button
                  type="button"
                  onClick={() => {
                    handleRandomizeName();
                    setShowNicknameModal(true);
                  }}
                  className="w-full py-3.5 bg-[#FFB300] hover:bg-[#FFA000] text-[#0D1117] font-black rounded-xl text-base tracking-wide transition shadow-xl shadow-[#FFB300]/25 flex items-center justify-center gap-2"
                >
                  <Check className="w-5 h-5 stroke-[3]" />
                  <span>PILIH {activeInfo.displayName.toUpperCase()} & BERI NAMA NICKNAME</span>
                </button>
              </div>
            );
          })()}

          {/* Nickname Input Modal Prompt */}
          {showNicknameModal && (
            <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4">
              <div className="bg-[#161B22] border-2 border-[#FFB300] rounded-3xl max-w-md w-full p-6 shadow-2xl relative">
                <button
                  type="button"
                  onClick={() => setShowNicknameModal(false)}
                  className="absolute top-4 right-4 text-slate-400 hover:text-white"
                >
                  <X className="w-5 h-5" />
                </button>

                <div className="text-center mb-6">
                  <div className="w-14 h-14 mx-auto mb-3 rounded-2xl bg-[#212836] border border-[#FFB300] flex items-center justify-center">
                    {renderClassIcon(selectedClass, 'w-7 h-7')}
                  </div>
                  <h3 className="text-xl font-black text-white">Beri Nama Karakter</h3>
                  <p className="text-xs text-slate-400 mt-1">
                    Class: <span className="text-[#FFD54F] font-bold">{HERO_CLASSES[selectedClass].displayName}</span>
                  </p>
                </div>

                {nicknameError && (
                  <div className="mb-4 p-2.5 bg-red-500/15 border border-red-500/30 rounded-xl text-red-400 text-xs">
                    {nicknameError}
                  </div>
                )}

                <div className="space-y-4 mb-6">
                  <div>
                    <label className="block text-xs font-semibold text-slate-400 mb-1">
                      Nickname Game (3-16 Karakter)
                    </label>
                    <input
                      type="text"
                      maxLength={16}
                      value={nicknameInput}
                      onChange={e => { setNicknameInput(e.target.value); setNicknameError(null); }}
                      placeholder="Masukkan nama panggilan..."
                      className="w-full bg-[#0D1117] border border-[#30363D] rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:border-[#FFB300]"
                    />
                  </div>

                  <button
                    type="button"
                    onClick={handleRandomizeName}
                    className="w-full py-2 bg-[#212836] hover:bg-[#2B3447] border border-[#FFB300]/30 text-[#FFD54F] rounded-xl text-xs font-bold flex items-center justify-center gap-2 transition"
                  >
                    <Dice5 className="w-4 h-4" />
                    <span>Acak Nama Keren RPG</span>
                  </button>
                </div>

                <div className="grid grid-cols-2 gap-3">
                  <button
                    type="button"
                    onClick={() => setShowNicknameModal(false)}
                    className="py-2.5 rounded-xl border border-[#30363D] text-slate-400 hover:text-white text-sm font-semibold transition"
                  >
                    Batal
                  </button>
                  <button
                    type="button"
                    onClick={handleConfirmCharacter}
                    className="py-2.5 rounded-xl bg-[#FFB300] hover:bg-[#FFA000] text-[#0D1117] font-black text-sm transition shadow-lg shadow-[#FFB300]/20"
                  >
                    Mulai Main
                  </button>
                </div>
              </div>
            </div>
          )}
        </div>
      )}

      {/* VIEW 3: MAIN GAME DASHBOARD */}
      {screen === 'DASHBOARD' && activeCharacter && (
        <div className="flex-1 flex flex-col max-w-4xl mx-auto w-full">
          {/* Top Bar with Character, Level, Gold & Diamonds */}
          <header className="bg-[#161B22] border-b border-[#30363D] p-3 sm:p-4 sticky top-0 z-30">
            <div className="flex items-center justify-between gap-2">
              {/* Character Persona Mini */}
              <div className="flex items-center gap-3 min-w-0">
                <div
                  className="w-11 h-11 rounded-xl flex items-center justify-center border-2 flex-shrink-0"
                  style={{
                    backgroundColor: `${HERO_CLASSES[activeCharacter.heroClass].colorHex}25`,
                    borderColor: HERO_CLASSES[activeCharacter.heroClass].glowHex
                  }}
                >
                  {renderClassIcon(activeCharacter.heroClass, 'w-6 h-6')}
                </div>
                <div className="min-w-0">
                  <div className="flex items-center gap-2">
                    <h2 className="font-extrabold text-sm sm:text-base text-white truncate">
                      {activeCharacter.nickname}
                    </h2>
                    <span className="px-1.5 py-0.5 rounded bg-[#FFB300] text-[#0D1117] font-black text-[10px]">
                      Lv.{activeCharacter.level}
                    </span>
                  </div>
                  <div className="flex items-center gap-1.5 text-[11px] text-slate-400">
                    <span className="font-semibold text-[#FFD54F]">
                      {HERO_CLASSES[activeCharacter.heroClass].displayName}
                    </span>
                    <span>•</span>
                    <span className="text-slate-300 font-bold">
                      CP: {calculatedStats.combatPower.toLocaleString()}
                    </span>
                  </div>
                </div>
              </div>

              {/* Currency Badges & Mission Indicator */}
              <div className="flex items-center gap-2">
                <button
                  type="button"
                  onClick={() => setActiveTab('MISSIONS')}
                  className="flex items-center gap-1.5 bg-[#212836] hover:bg-[#2B3447] border border-[#FFB300]/40 px-2.5 py-1.5 rounded-xl text-xs font-bold text-[#FFD54F] transition relative"
                  title="Lihat Papan Misi Harian"
                >
                  <Trophy className="w-3.5 h-3.5 text-[#FFB300]" />
                  <span>{completedMissionsCount}/5</span>
                  {unclaimedMissionsCount > 0 && (
                    <span className="w-2 h-2 rounded-full bg-red-500 animate-pulse absolute -top-0.5 -right-0.5" />
                  )}
                </button>

                <div className="flex items-center gap-1.5 bg-[#212836] border border-[#FFB300]/40 px-2.5 py-1.5 rounded-xl text-xs font-bold text-[#FFD54F]">
                  <Coins className="w-4 h-4 text-[#FFB300]" />
                  <span>{activeCharacter.gold.toLocaleString()}</span>
                </div>
                <div className="flex items-center gap-1.5 bg-[#212836] border border-purple-500/40 px-2.5 py-1.5 rounded-xl text-xs font-bold text-purple-300">
                  <Gem className="w-4 h-4 text-purple-400" />
                  <span>{activeCharacter.diamonds}</span>
                </div>
              </div>
            </div>

            {/* EXP Bar */}
            <div className="mt-2.5 flex items-center gap-2 text-[10px]">
              <span className="font-bold text-slate-400 uppercase tracking-wider">EXP</span>
              <div className="flex-1 h-2 bg-[#212836] rounded-full overflow-hidden">
                <div
                  className="h-full bg-[#FFB300] rounded-full transition-all duration-300"
                  style={{ width: `${Math.min(100, (activeCharacter.currentExp / activeCharacter.maxExp) * 100)}%` }}
                />
              </div>
              <span className="text-slate-400 font-semibold">{activeCharacter.currentExp}/{activeCharacter.maxExp}</span>
            </div>
          </header>

          {/* Navigation Tabs Header */}
          <div className="bg-[#161B22] border-b border-[#30363D] grid grid-cols-5 px-1 sm:px-2">
            <button
              type="button"
              onClick={() => setActiveTab('OVERVIEW')}
              className={`py-3 text-[11px] sm:text-xs font-bold border-b-2 transition flex items-center justify-center gap-1 sm:gap-1.5 ${
                activeTab === 'OVERVIEW'
                  ? 'border-[#FFB300] text-[#FFB300]'
                  : 'border-transparent text-slate-400 hover:text-white'
              }`}
            >
              <User className="w-3.5 h-3.5 sm:w-4 sm:h-4" />
              <span>Karakter</span>
            </button>

            <button
              type="button"
              onClick={() => setActiveTab('INVENTORY')}
              className={`py-3 text-[11px] sm:text-xs font-bold border-b-2 transition flex items-center justify-center gap-1 sm:gap-1.5 ${
                activeTab === 'INVENTORY'
                  ? 'border-[#FFB300] text-[#FFB300]'
                  : 'border-transparent text-slate-400 hover:text-white'
              }`}
            >
              <Backpack className="w-3.5 h-3.5 sm:w-4 sm:h-4" />
              <span>Tas</span>
            </button>

            <button
              type="button"
              onClick={() => setActiveTab('MISSIONS')}
              className={`py-3 text-[11px] sm:text-xs font-bold border-b-2 transition flex items-center justify-center gap-1 sm:gap-1.5 relative ${
                activeTab === 'MISSIONS'
                  ? 'border-[#FFB300] text-[#FFB300]'
                  : 'border-transparent text-slate-400 hover:text-white'
              }`}
            >
              <Calendar className="w-3.5 h-3.5 sm:w-4 sm:h-4" />
              <span>Misi</span>
              {unclaimedMissionsCount > 0 && (
                <span className="w-2 h-2 rounded-full bg-red-500 animate-ping absolute top-2 right-1 sm:right-2" />
              )}
              {unclaimedMissionsCount > 0 && (
                <span className="px-1 py-0.2 rounded-full bg-red-500 text-white text-[9px] font-black absolute top-1 right-1 sm:right-2">
                  {unclaimedMissionsCount}
                </span>
              )}
            </button>

            <button
              type="button"
              onClick={() => setActiveTab('DUNGEON')}
              className={`py-3 text-[11px] sm:text-xs font-bold border-b-2 transition flex items-center justify-center gap-1 sm:gap-1.5 ${
                activeTab === 'DUNGEON'
                  ? 'border-[#FFB300] text-[#FFB300]'
                  : 'border-transparent text-slate-400 hover:text-white'
              }`}
            >
              <Activity className="w-3.5 h-3.5 sm:w-4 sm:h-4" />
              <span>Dungeon</span>
            </button>

            <button
              type="button"
              onClick={() => setActiveTab('ACCOUNT')}
              className={`py-3 text-[11px] sm:text-xs font-bold border-b-2 transition flex items-center justify-center gap-1 sm:gap-1.5 ${
                activeTab === 'ACCOUNT'
                  ? 'border-[#FFB300] text-[#FFB300]'
                  : 'border-transparent text-slate-400 hover:text-white'
              }`}
            >
              <RotateCcw className="w-3.5 h-3.5 sm:w-4 sm:h-4" />
              <span>Akun</span>
            </button>
          </div>

          {/* TAB CONTENT */}
          <main className="flex-1 overflow-y-auto p-4 sm:p-6">
            {/* TAB 1: KARAKTER & STATISTIK DASAR */}
            {activeTab === 'OVERVIEW' && (
              <div className="space-y-6">
                {/* Hero Showcase Card */}
                <div className="bg-[#161B22] border border-[#30363D] rounded-2xl p-5 flex flex-col sm:flex-row items-center justify-between gap-4">
                  <div className="flex items-center gap-4">
                    <div
                      className="w-16 h-16 rounded-2xl flex items-center justify-center border-2 shadow-lg"
                      style={{
                        backgroundColor: `${HERO_CLASSES[activeCharacter.heroClass].colorHex}25`,
                        borderColor: HERO_CLASSES[activeCharacter.heroClass].glowHex
                      }}
                    >
                      {renderClassIcon(activeCharacter.heroClass, 'w-8 h-8')}
                    </div>
                    <div>
                      <h3 className="text-xl font-black text-white">{activeCharacter.nickname}</h3>
                      <p className="text-xs text-[#FFD54F] font-bold">
                        {HERO_CLASSES[activeCharacter.heroClass].title}
                      </p>
                      <p className="text-[11px] text-slate-400 mt-0.5">
                        {HERO_CLASSES[activeCharacter.heroClass].mainAdvantage}
                      </p>
                    </div>
                  </div>

                  <div className="bg-[#212836] border border-[#FFB300]/50 px-5 py-3 rounded-2xl text-center">
                    <span className="text-[10px] uppercase font-extrabold text-slate-400 tracking-wider block">
                      TOTAL COMBAT POWER
                    </span>
                    <span className="rpg-font text-2xl font-black text-[#FFB300]">
                      {calculatedStats.combatPower.toLocaleString()}
                    </span>
                  </div>
                </div>

                {/* Daily Mission Shortcut Card on Overview */}
                <div
                  onClick={() => setActiveTab('MISSIONS')}
                  className="bg-[#161B22] hover:bg-[#1C2333] border border-[#FFB300]/40 rounded-2xl p-4 flex items-center justify-between cursor-pointer transition shadow-md group"
                >
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-xl bg-[#212836] border border-[#FFB300]/60 flex items-center justify-center text-[#FFB300] flex-shrink-0">
                      <Trophy className="w-5 h-5" />
                    </div>
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="text-sm font-bold text-white group-hover:text-[#FFD54F] transition">
                          Misi Harian Hari Ini
                        </span>
                        <span className="text-[10px] font-extrabold px-2 py-0.5 rounded-full bg-[#FFB300]/20 text-[#FFD54F] border border-[#FFB300]/30">
                          {completedMissionsCount}/5 Selesai
                        </span>
                      </div>
                      <p className="text-xs text-slate-400 mt-0.5">
                        {unclaimedMissionsCount > 0
                          ? `🎉 Ada ${unclaimedMissionsCount} hadiah misi siap kamu klaim!`
                          : completedMissionsCount === 5
                          ? '✨ Semua misi selesai! Buka peti hadiah spesial di tab Misi.'
                          : 'Selesaikan tugas harian untuk ekstra Gold & EXP.'}
                      </p>
                    </div>
                  </div>
                  <div className="flex items-center gap-1 text-xs font-bold text-[#FFB300]">
                    <span>Buka Misi</span>
                    <ChevronRight className="w-4 h-4" />
                  </div>
                </div>

                {/* STATISTIK DASAR LENGKAP YANG TERLIHAT JELAS (User requirement) */}
                <div className="bg-[#161B22] border border-[#30363D] rounded-2xl p-5">
                  <div className="flex items-center justify-between mb-4">
                    <h3 className="text-xs font-black uppercase tracking-wider text-[#FFB300]">
                      STATISTIK DASAR KARAKTER
                    </h3>
                    <span className="text-[11px] text-slate-400 font-semibold">
                      Termasuk Buff Equipment Terpasang
                    </span>
                  </div>

                  <div className="space-y-3">
                    {/* HP */}
                    <div className="bg-[#212836] p-3 rounded-xl">
                      <div className="flex justify-between items-center text-xs mb-1.5">
                        <span className="text-slate-300 font-bold">Health Points (HP)</span>
                        <div className="flex items-center gap-1.5">
                          <span className="font-extrabold text-red-400 text-sm">{calculatedStats.hp}</span>
                          {calculatedStats.bonusHp > 0 && (
                            <span className="text-[10px] font-bold text-emerald-400">
                              (+{calculatedStats.bonusHp})
                            </span>
                          )}
                        </div>
                      </div>
                      <div className="h-2 bg-[#0D1117] rounded-full overflow-hidden">
                        <div
                          className="h-full bg-gradient-to-r from-red-600 to-red-400 rounded-full"
                          style={{ width: `${Math.min(100, (calculatedStats.hp / 2500) * 100)}%` }}
                        />
                      </div>
                    </div>

                    {/* MP */}
                    <div className="bg-[#212836] p-3 rounded-xl">
                      <div className="flex justify-between items-center text-xs mb-1.5">
                        <span className="text-slate-300 font-bold">Mana Points (MP)</span>
                        <div className="flex items-center gap-1.5">
                          <span className="font-extrabold text-blue-400 text-sm">{calculatedStats.mp}</span>
                          {calculatedStats.bonusMp > 0 && (
                            <span className="text-[10px] font-bold text-emerald-400">
                              (+{calculatedStats.bonusMp})
                            </span>
                          )}
                        </div>
                      </div>
                      <div className="h-2 bg-[#0D1117] rounded-full overflow-hidden">
                        <div
                          className="h-full bg-gradient-to-r from-blue-600 to-blue-400 rounded-full"
                          style={{ width: `${Math.min(100, (calculatedStats.mp / 2200) * 100)}%` }}
                        />
                      </div>
                    </div>

                    {/* Physical ATK */}
                    <div className="bg-[#212836] p-3 rounded-xl">
                      <div className="flex justify-between items-center text-xs mb-1.5">
                        <span className="text-slate-300 font-bold">Serangan Fisik (Physical ATK)</span>
                        <div className="flex items-center gap-1.5">
                          <span className="font-extrabold text-amber-400 text-sm">{calculatedStats.physicalAtk}</span>
                          {calculatedStats.bonusAtk > 0 && (
                            <span className="text-[10px] font-bold text-emerald-400">
                              (+{calculatedStats.bonusAtk})
                            </span>
                          )}
                        </div>
                      </div>
                      <div className="h-2 bg-[#0D1117] rounded-full overflow-hidden">
                        <div
                          className="h-full bg-gradient-to-r from-amber-600 to-amber-400 rounded-full"
                          style={{ width: `${Math.min(100, (calculatedStats.physicalAtk / 350) * 100)}%` }}
                        />
                      </div>
                    </div>

                    {/* Magic ATK */}
                    <div className="bg-[#212836] p-3 rounded-xl">
                      <div className="flex justify-between items-center text-xs mb-1.5">
                        <span className="text-slate-300 font-bold">Serangan Sihir (Magic ATK)</span>
                        <div className="flex items-center gap-1.5">
                          <span className="font-extrabold text-purple-400 text-sm">{calculatedStats.magicAtk}</span>
                          {calculatedStats.bonusMatk > 0 && (
                            <span className="text-[10px] font-bold text-emerald-400">
                              (+{calculatedStats.bonusMatk})
                            </span>
                          )}
                        </div>
                      </div>
                      <div className="h-2 bg-[#0D1117] rounded-full overflow-hidden">
                        <div
                          className="h-full bg-gradient-to-r from-purple-600 to-purple-400 rounded-full"
                          style={{ width: `${Math.min(100, (calculatedStats.magicAtk / 350) * 100)}%` }}
                        />
                      </div>
                    </div>

                    {/* DEF */}
                    <div className="bg-[#212836] p-3 rounded-xl">
                      <div className="flex justify-between items-center text-xs mb-1.5">
                        <span className="text-slate-300 font-bold">Pertahanan (DEF)</span>
                        <div className="flex items-center gap-1.5">
                          <span className="font-extrabold text-teal-400 text-sm">{calculatedStats.def}</span>
                          {calculatedStats.bonusDef > 0 && (
                            <span className="text-[10px] font-bold text-emerald-400">
                              (+{calculatedStats.bonusDef})
                            </span>
                          )}
                        </div>
                      </div>
                      <div className="h-2 bg-[#0D1117] rounded-full overflow-hidden">
                        <div
                          className="h-full bg-gradient-to-r from-teal-600 to-teal-400 rounded-full"
                          style={{ width: `${Math.min(100, (calculatedStats.def / 300) * 100)}%` }}
                        />
                      </div>
                    </div>

                    {/* Crit Rate */}
                    <div className="bg-[#212836] p-3 rounded-xl">
                      <div className="flex justify-between items-center text-xs mb-1.5">
                        <span className="text-slate-300 font-bold">Peluang Kritis (Crit Rate)</span>
                        <div className="flex items-center gap-1.5">
                          <span className="font-extrabold text-rose-400 text-sm">{calculatedStats.critRate}%</span>
                          {calculatedStats.bonusCrit > 0 && (
                            <span className="text-[10px] font-bold text-emerald-400">
                              (+{calculatedStats.bonusCrit}%)
                            </span>
                          )}
                        </div>
                      </div>
                      <div className="h-2 bg-[#0D1117] rounded-full overflow-hidden">
                        <div
                          className="h-full bg-gradient-to-r from-rose-600 to-rose-400 rounded-full"
                          style={{ width: `${Math.min(100, (calculatedStats.critRate / 100) * 100)}%` }}
                        />
                      </div>
                    </div>

                    {/* Speed */}
                    <div className="bg-[#212836] p-3 rounded-xl">
                      <div className="flex justify-between items-center text-xs mb-1.5">
                        <span className="text-slate-300 font-bold">Kecepatan (Speed / Agility)</span>
                        <div className="flex items-center gap-1.5">
                          <span className="font-extrabold text-yellow-400 text-sm">{calculatedStats.speed}</span>
                          {calculatedStats.bonusSpeed > 0 && (
                            <span className="text-[10px] font-bold text-emerald-400">
                              (+{calculatedStats.bonusSpeed})
                            </span>
                          )}
                        </div>
                      </div>
                      <div className="h-2 bg-[#0D1117] rounded-full overflow-hidden">
                        <div
                          className="h-full bg-gradient-to-r from-yellow-600 to-yellow-400 rounded-full"
                          style={{ width: `${Math.min(100, (calculatedStats.speed / 200) * 100)}%` }}
                        />
                      </div>
                    </div>
                  </div>
                </div>

                {/* Passive Skill Active Card */}
                <div className="bg-[#161B22] border border-[#30363D] rounded-2xl p-4">
                  <div className="flex items-center gap-2 text-[#FFB300] font-bold text-sm mb-1.5">
                    <Zap className="w-4 h-4 fill-current" />
                    <span>Pasif Aktif: {HERO_CLASSES[activeCharacter.heroClass].passiveName}</span>
                  </div>
                  <p className="text-xs text-slate-300 leading-relaxed font-medium">
                    {HERO_CLASSES[activeCharacter.heroClass].passiveDescription}
                  </p>
                </div>

                {/* Quick Train Button */}
                <button
                  type="button"
                  onClick={handleTrainCharacter}
                  className="w-full py-3.5 bg-[#FFB300] hover:bg-[#FFA000] text-[#0D1117] font-black rounded-xl text-sm transition shadow-lg shadow-[#FFB300]/20 flex items-center justify-center gap-2"
                >
                  <Dumbbell className="w-5 h-5" />
                  <span>LATIHAN KARAKTER (+45 EXP, +80 GOLD)</span>
                </button>
              </div>
            )}

            {/* TAB 2: INVENTORY & EQUIPMENT SYSTEM */}
            {activeTab === 'INVENTORY' && (
              <div className="space-y-6">
                {/* 5 Equipped Gear Slots */}
                <div className="bg-[#161B22] border border-[#30363D] rounded-2xl p-5">
                  <h3 className="text-xs font-black uppercase tracking-wider text-[#FFB300] mb-3">
                    EQUIPMENT TERPASANG
                  </h3>

                  <div className="grid grid-cols-5 gap-2 sm:gap-3">
                    {(['WEAPON', 'ARMOR', 'HELMET', 'BOOTS', 'ACCESSORY'] as EquipmentType[]).map(slotType => {
                      const item = equippedItems.find(it => it.type === slotType);
                      return (
                        <button
                          key={slotType}
                          type="button"
                          onClick={() => item && setInspectedItem(item)}
                          className={`p-2 sm:p-3 rounded-xl border text-center flex flex-col items-center justify-center min-h-[80px] sm:min-h-[96px] transition ${
                            item
                              ? 'bg-[#212836] border-[#FFB300]/60 hover:border-[#FFB300]'
                              : 'bg-[#0D1117] border-[#30363D] opacity-60 cursor-default'
                          }`}
                        >
                          <div className="w-8 h-8 rounded-lg bg-[#161B22] flex items-center justify-center mb-1 text-[#FFB300]">
                            <Sword className="w-4 h-4" />
                          </div>
                          <span className="text-[10px] font-bold text-slate-300 truncate w-full">
                            {item ? item.name : slotType}
                          </span>
                          <span className="text-[9px] text-[#FFD54F] font-semibold">
                            {item ? item.rarity : 'Kosong'}
                          </span>
                        </button>
                      );
                    })}
                  </div>
                </div>

                {/* Filter and Chest Action */}
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                  {/* Filters */}
                  <div className="flex flex-wrap gap-1.5">
                    {(['ALL', 'WEAPON', 'ARMOR', 'ACCESSORY'] as const).map(filter => (
                      <button
                        key={filter}
                        type="button"
                        onClick={() => setSelectedInventoryFilter(filter)}
                        className={`px-3 py-1.5 rounded-lg text-xs font-bold transition ${
                          selectedInventoryFilter === filter
                            ? 'bg-[#FFB300] text-[#0D1117]'
                            : 'bg-[#212836] text-slate-400 hover:text-white'
                        }`}
                      >
                        {filter === 'ALL' ? 'Semua' : filter}
                      </button>
                    ))}
                  </div>

                  {/* Open Loot Chest */}
                  <button
                    type="button"
                    onClick={handleOpenLootChest}
                    className="px-4 py-2 bg-gradient-to-r from-amber-500 to-yellow-500 hover:from-amber-600 hover:to-yellow-600 text-[#0D1117] font-black rounded-xl text-xs flex items-center gap-2 shadow-lg transition self-start sm:self-auto"
                  >
                    <Gift className="w-4 h-4" />
                    <span>Buka Peti Misteri (250 Gold)</span>
                  </button>
                </div>

                {/* Inventory Bag List */}
                <div className="space-y-2">
                  {currentInventory
                    .filter(item => selectedInventoryFilter === 'ALL' || item.type === selectedInventoryFilter)
                    .map(item => (
                      <div
                        key={item.id}
                        onClick={() => setInspectedItem(item)}
                        className="bg-[#161B22] hover:bg-[#212836] border border-[#30363D] hover:border-[#FFB300]/60 p-3.5 rounded-xl flex items-center justify-between gap-3 cursor-pointer transition"
                      >
                        <div className="flex items-center gap-3 min-w-0">
                          <div className="w-10 h-10 rounded-lg bg-[#0D1117] border border-[#30363D] flex items-center justify-center text-[#FFB300] flex-shrink-0">
                            <Sword className="w-5 h-5" />
                          </div>
                          <div className="min-w-0">
                            <div className="flex items-center gap-2">
                              <span className="font-bold text-sm text-white truncate">{item.name}</span>
                              {item.isEquipped && (
                                <span className="px-1.5 py-0.5 rounded bg-[#FFB300] text-[#0D1117] font-black text-[9px]">
                                  DIPAKAI
                                </span>
                              )}
                            </div>
                            <div className="flex items-center gap-2 mt-0.5">
                              <span className={`px-2 py-0.5 text-[9px] font-black rounded border ${getRarityBadgeColor(item.rarity)}`}>
                                {item.rarity}
                              </span>
                              <span className="text-[11px] text-slate-400">{item.type}</span>
                            </div>
                          </div>
                        </div>

                        {/* Quick Stat Summary */}
                        <div className="text-right flex-shrink-0">
                          <div className="text-xs font-bold text-emerald-400">
                            {item.atkBonus > 0 && `+${item.atkBonus} ATK `}
                            {item.matkBonus > 0 && `+${item.matkBonus} MATK `}
                            {item.defBonus > 0 && `+${item.defBonus} DEF `}
                            {item.critBonus > 0 && `+${item.critBonus}% CRIT `}
                          </div>
                          <ChevronRight className="w-4 h-4 text-slate-500 inline-block mt-1" />
                        </div>
                      </div>
                    ))}
                </div>
              </div>
            )}

            {/* TAB: DAILY MISSIONS */}
            {activeTab === 'MISSIONS' && (
              <div className="space-y-6">
                {/* Header Banner */}
                <div className="bg-[#161B22] border border-[#30363D] rounded-2xl p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                  <div>
                    <div className="flex items-center gap-2">
                      <Trophy className="w-5 h-5 text-[#FFB300]" />
                      <h3 className="text-base font-black text-white">PAPAN MISI HARIAN (DAILY MISSIONS)</h3>
                    </div>
                    <p className="text-xs text-slate-400 mt-1">
                      Selesaikan 5 tugas sederhana setiap hari untuk mengumpulkan tumpukan Gold dan EXP ekstra!
                    </p>
                  </div>

                  <button
                    type="button"
                    onClick={handleResetDailyMissions}
                    className="self-start sm:self-auto px-3 py-1.5 bg-[#212836] hover:bg-[#2B3447] text-slate-300 hover:text-white border border-[#30363D] rounded-xl text-xs font-bold flex items-center gap-1.5 transition"
                    title="Reset misi untuk mengulang tugas hari ini"
                  >
                    <RotateCcw className="w-3.5 h-3.5" />
                    <span>Reset Hari Baru</span>
                  </button>
                </div>

                {/* Grand Daily Completion Chest Card */}
                <div className="bg-gradient-to-r from-[#212836] via-[#1A2230] to-[#212836] border-2 border-[#FFB300]/70 rounded-2xl p-5 shadow-xl relative overflow-hidden">
                  <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
                    <div className="flex items-center gap-4">
                      <div className="w-14 h-14 rounded-2xl bg-[#0D1117] border-2 border-[#FFB300] flex items-center justify-center text-[#FFB300] shadow-[0_0_20px_rgba(255,179,0,0.25)] flex-shrink-0">
                        <Gift className="w-7 h-7" />
                      </div>
                      <div>
                        <div className="flex items-center gap-2">
                          <span className="text-base font-black text-white">PETI PENCAPAIAN HARIAN</span>
                          <span className="px-2 py-0.5 rounded-full bg-[#FFB300]/20 text-[#FFD54F] font-black text-[10px] border border-[#FFB300]/40">
                            GRAND REWARD
                          </span>
                        </div>
                        <p className="text-xs text-slate-300 mt-0.5">
                          Selesaikan kelima misi harian untuk membuka peti bonus spesial!
                        </p>
                        <div className="flex items-center gap-3 mt-2 text-xs font-extrabold">
                          <span className="text-[#FFD54F] flex items-center gap-1">
                            <Coins className="w-3.5 h-3.5" /> +600 Gold
                          </span>
                          <span className="text-emerald-400">
                            +250 EXP
                          </span>
                          <span className="text-purple-300 flex items-center gap-1">
                            <Gem className="w-3.5 h-3.5" /> +15 Ruby
                          </span>
                        </div>
                      </div>
                    </div>

                    <div className="w-full sm:w-auto flex-shrink-0 text-center sm:text-right">
                      {dailyChestClaimed ? (
                        <div className="px-4 py-2 bg-emerald-500/15 border border-emerald-500/40 rounded-xl text-emerald-400 font-bold text-xs flex items-center justify-center gap-2">
                          <CheckCircle2 className="w-4 h-4" />
                          <span>Peti Sudah Diklaim Hari Ini</span>
                        </div>
                      ) : completedMissionsCount >= 5 ? (
                        <button
                          type="button"
                          onClick={handleClaimDailyChest}
                          className="w-full sm:w-auto px-5 py-3 bg-[#FFB300] hover:bg-[#FFA000] text-[#0D1117] font-black rounded-xl text-xs transition shadow-lg shadow-[#FFB300]/30 animate-bounce flex items-center justify-center gap-2"
                        >
                          <Gift className="w-4 h-4" />
                          <span>KLAIM PETI HARIAN SPESIAL!</span>
                        </button>
                      ) : (
                        <div className="text-xs font-semibold text-slate-400 bg-[#0D1117] px-4 py-2 rounded-xl border border-[#30363D]">
                          Progres: <span className="text-[#FFB300] font-black">{completedMissionsCount}/5</span> Selesai
                        </div>
                      )}
                    </div>
                  </div>

                  {/* Progress bar towards Grand Chest */}
                  <div className="mt-4 pt-3 border-t border-[#30363D]/60 flex items-center gap-3 text-[11px]">
                    <span className="font-bold text-slate-400">Pencapaian:</span>
                    <div className="flex-1 h-2 bg-[#0D1117] rounded-full overflow-hidden">
                      <div
                        className="h-full bg-gradient-to-r from-amber-500 to-[#FFD54F] rounded-full transition-all duration-500"
                        style={{ width: `${(completedMissionsCount / 5) * 100}%` }}
                      />
                    </div>
                    <span className="font-bold text-[#FFD54F]">{completedMissionsCount} dari 5 Misi Selesai</span>
                  </div>
                </div>

                {/* List of 5 Daily Missions */}
                <div className="space-y-3">
                  {dailyMissions.map(mission => {
                    const isDone = mission.currentCount >= mission.targetCount;
                    const canClaim = isDone && !mission.isClaimed;

                    return (
                      <div
                        key={mission.id}
                        className={`bg-[#161B22] border rounded-2xl p-4 transition flex flex-col sm:flex-row sm:items-center justify-between gap-4 ${
                          canClaim
                            ? 'border-[#FFB300] shadow-[0_0_15px_rgba(255,179,0,0.15)] bg-[#1A2230]'
                            : mission.isClaimed
                            ? 'border-[#30363D] opacity-75'
                            : 'border-[#30363D] hover:border-slate-500'
                        }`}
                      >
                        <div className="flex items-start gap-3.5">
                          <div
                            className={`w-11 h-11 rounded-xl flex items-center justify-center flex-shrink-0 border ${
                              canClaim
                                ? 'bg-[#FFB300]/20 border-[#FFB300] text-[#FFD54F]'
                                : mission.isClaimed
                                ? 'bg-emerald-500/15 border-emerald-500/40 text-emerald-400'
                                : 'bg-[#212836] border-[#30363D] text-slate-400'
                            }`}
                          >
                            {mission.category === 'LOGIN' && <Calendar className="w-5 h-5" />}
                            {mission.category === 'TRAIN' && <Dumbbell className="w-5 h-5" />}
                            {mission.category === 'DUNGEON' && <Activity className="w-5 h-5" />}
                            {mission.category === 'EQUIP' && <Sword className="w-5 h-5" />}
                            {mission.category === 'CHEST' && <Gift className="w-5 h-5" />}
                          </div>

                          <div className="min-w-0">
                            <div className="flex items-center gap-2">
                              <h4 className="font-bold text-sm text-white">{mission.title}</h4>
                              {mission.isClaimed && (
                                <span className="px-1.5 py-0.5 rounded bg-emerald-500/20 text-emerald-400 text-[10px] font-bold flex items-center gap-1">
                                  <Check className="w-3 h-3" /> Diklaim
                                </span>
                              )}
                            </div>
                            <p className="text-xs text-slate-400 mt-0.5">{mission.description}</p>

                            {/* Rewards and Progress */}
                            <div className="flex flex-wrap items-center gap-2.5 mt-2.5">
                              <span className="text-[11px] font-bold px-2 py-0.5 rounded bg-[#212836] border border-[#FFB300]/30 text-[#FFD54F] flex items-center gap-1">
                                <Coins className="w-3 h-3 text-[#FFB300]" /> +{mission.goldReward} Gold
                              </span>
                              <span className="text-[11px] font-bold px-2 py-0.5 rounded bg-[#212836] border border-emerald-500/30 text-emerald-400">
                                +{mission.expReward} EXP
                              </span>
                              {mission.diamondReward && (
                                <span className="text-[11px] font-bold px-2 py-0.5 rounded bg-[#212836] border border-purple-500/30 text-purple-300 flex items-center gap-1">
                                  <Gem className="w-3 h-3" /> +{mission.diamondReward} Ruby
                                </span>
                              )}
                            </div>
                          </div>
                        </div>

                        {/* Action buttons */}
                        <div className="sm:text-right flex items-center sm:flex-col sm:items-end justify-between gap-2 border-t sm:border-t-0 pt-3 sm:pt-0 border-[#30363D]">
                          <div className="text-xs font-semibold text-slate-400">
                            Progres: <span className="font-extrabold text-white">{mission.currentCount}/{mission.targetCount}</span>
                          </div>

                          {mission.isClaimed ? (
                            <div className="px-3 py-1.5 rounded-xl bg-[#212836] text-slate-400 text-xs font-semibold flex items-center gap-1.5">
                              <CheckCircle2 className="w-4 h-4 text-emerald-400" />
                              <span>Selesai</span>
                            </div>
                          ) : canClaim ? (
                            <button
                              type="button"
                              onClick={() => handleClaimMission(mission.id)}
                              className="px-4 py-2 bg-[#FFB300] hover:bg-[#FFA000] text-[#0D1117] font-black rounded-xl text-xs transition shadow-md shadow-[#FFB300]/25 flex items-center gap-1.5 animate-pulse"
                            >
                              <Sparkles className="w-3.5 h-3.5" />
                              <span>KLAIM HADIAH</span>
                            </button>
                          ) : (
                            <button
                              type="button"
                              onClick={() => {
                                if (mission.category === 'TRAIN') {
                                  handleTrainCharacter();
                                } else if (mission.category === 'DUNGEON') {
                                  setActiveTab('DUNGEON');
                                } else if (mission.category === 'EQUIP' || mission.category === 'CHEST') {
                                  setActiveTab('INVENTORY');
                                }
                              }}
                              className="px-3.5 py-1.5 bg-[#212836] hover:bg-[#2B3447] text-[#FFD54F] border border-[#FFB300]/40 rounded-xl text-xs font-bold transition flex items-center gap-1"
                            >
                              <span>{mission.category === 'TRAIN' ? 'Latihan Sekarang' : 'Kerjakan'}</span>
                              <ChevronRight className="w-3.5 h-3.5" />
                            </button>
                          )}
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>
            )}

            {/* TAB 3: DUNGEON ARENA */}
            {activeTab === 'DUNGEON' && (
              <div className="space-y-6">
                {/* Dungeon Header and Action */}
                <div className="bg-[#161B22] border border-[#30363D] rounded-2xl p-5 shadow-lg">
                  <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-4">
                    <div>
                      <div className="flex items-center gap-2">
                        <Swords className="w-5 h-5 text-[#FFB300]" />
                        <h3 className="text-base font-black text-white">RUANG BAWAH TANAH KUNO</h3>
                      </div>
                      <p className="text-xs text-slate-400 mt-0.5">
                        Uji keunggulan class <span className="text-[#FFD54F] font-bold">{HERO_CLASSES[activeCharacter.heroClass].displayName}</span> dalam pertarungan monster!
                      </p>
                    </div>

                    <div className="flex items-center gap-2">
                      <span className="text-[11px] font-bold px-3 py-1 rounded-full bg-[#212836] border border-[#FFB300]/30 text-[#FFD54F]">
                        Pasif: {HERO_CLASSES[activeCharacter.heroClass].passiveName}
                      </span>
                    </div>
                  </div>

                  <button
                    type="button"
                    disabled={isBattling}
                    onClick={handleStartDungeon}
                    className="w-full py-3.5 bg-[#FFB300] hover:bg-[#FFA000] disabled:bg-[#30363D] text-[#0D1117] font-black rounded-xl text-sm transition shadow-lg shadow-[#FFB300]/20 flex items-center justify-center gap-2 disabled:cursor-not-allowed"
                  >
                    {isBattling ? (
                      <>
                        <div className="w-4 h-4 border-2 border-[#0D1117] border-t-transparent rounded-full animate-spin" />
                        <span>Pertarungan Sedang Berlangsung...</span>
                      </>
                    ) : (
                      <>
                        <Play className="w-4 h-4 fill-current" />
                        <span>TANTANG MONSTER RUANG BAWAH TANAH</span>
                      </>
                    )}
                  </button>
                </div>

                {/* BATTLE ARENA STAGE (HERO vs MONSTER) */}
                <div className="bg-gradient-to-b from-[#1C2333] to-[#161B22] border border-[#30363D] rounded-2xl p-4 sm:p-5 shadow-xl relative overflow-hidden">
                  <div className="text-[10px] uppercase font-black tracking-widest text-slate-400 mb-3 flex items-center justify-between">
                    <span>ARENA TEMPUR DUEL</span>
                    <span className={isBattling ? 'text-amber-400 font-extrabold animate-pulse' : 'text-slate-500'}>
                      {isBattling ? '● COMBAT ACTIVE' : '○ STANDBY'}
                    </span>
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4 items-center">
                    {/* HERO COMBAT CARD with FRAMER MOTION SHAKE EFFECT */}
                    <motion.div
                      animate={
                        isHeroTakingDamage
                          ? {
                              x: [-12, 12, -9, 9, -5, 5, -2, 2, 0],
                              y: [-3, 3, -2, 2, 0],
                              rotate: [-2, 2, -1.2, 1.2, 0],
                            }
                          : { x: 0, y: 0, rotate: 0 }
                      }
                      transition={{ duration: 0.45, ease: 'easeInOut' }}
                      className={`relative bg-[#0D1117]/95 rounded-2xl p-4 border-2 transition-colors duration-200 overflow-hidden ${
                        isHeroTakingDamage
                          ? 'border-red-500 shadow-[0_0_30px_rgba(239,68,68,0.45)] bg-red-950/25 ring-2 ring-red-500/50'
                          : 'border-[#30363D] hover:border-slate-500 shadow-md'
                      }`}
                    >
                      {/* Floating Damage Taken Indicator on Hero */}
                      <AnimatePresence>
                        {isHeroTakingDamage && heroDamageTaken && (
                          <motion.div
                            initial={{ opacity: 0, y: 0, scale: 0.8 }}
                            animate={{ opacity: 1, y: -26, scale: 1.25 }}
                            exit={{ opacity: 0, y: -45, scale: 1 }}
                            transition={{ duration: 0.55 }}
                            className="absolute top-2 right-3 z-20 px-2.5 py-1 bg-red-600/90 text-white font-black text-xs sm:text-sm rounded-lg shadow-xl border border-red-400 flex items-center gap-1"
                          >
                            <span>💥 -{heroDamageTaken} DMG</span>
                          </motion.div>
                        )}
                      </AnimatePresence>

                      <div className="flex items-center gap-3 mb-3">
                        <div
                          className="w-12 h-12 rounded-xl flex items-center justify-center border-2 shadow flex-shrink-0"
                          style={{
                            backgroundColor: `${HERO_CLASSES[activeCharacter.heroClass].colorHex}25`,
                            borderColor: HERO_CLASSES[activeCharacter.heroClass].glowHex,
                          }}
                        >
                          {renderClassIcon(activeCharacter.heroClass, 'w-6 h-6')}
                        </div>
                        <div className="min-w-0">
                          <div className="flex items-center gap-1.5">
                            <span className="font-black text-white text-base truncate">{activeCharacter.nickname}</span>
                            <span className="text-[10px] font-bold px-1.5 py-0.5 rounded bg-[#FFB300]/20 text-[#FFD54F]">
                              Lv.{activeCharacter.level}
                            </span>
                          </div>
                          <span className="text-xs text-[#FFD54F] font-bold block">
                            {HERO_CLASSES[activeCharacter.heroClass].displayName}
                          </span>
                        </div>
                      </div>

                      {/* Hero HP Bar */}
                      <div className="space-y-1">
                        <div className="flex justify-between text-[11px] font-bold">
                          <span className="text-slate-400">HP Karakter</span>
                          <span className={heroCurrentHp < calculatedStats.hp * 0.4 ? 'text-red-400 animate-pulse' : 'text-emerald-400'}>
                            {heroCurrentHp} / {calculatedStats.hp}
                          </span>
                        </div>
                        <div className="h-2.5 bg-[#161B22] rounded-full overflow-hidden border border-[#30363D]">
                          <motion.div
                            className="h-full bg-gradient-to-r from-red-600 via-rose-500 to-emerald-400 rounded-full"
                            animate={{ width: `${Math.max(0, Math.min(100, (heroCurrentHp / calculatedStats.hp) * 100))}%` }}
                            transition={{ duration: 0.3 }}
                          />
                        </div>
                      </div>

                      <div className="mt-3 pt-2.5 border-t border-[#30363D] flex items-center justify-between text-[11px] text-slate-400">
                        <span>DEF: <strong className="text-white">{calculatedStats.def}</strong></span>
                        <span>ATK: <strong className="text-white">{calculatedStats.physicalAtk || calculatedStats.magicAtk}</strong></span>
                        <span>CRIT: <strong className="text-amber-400">{calculatedStats.critRate}%</strong></span>
                      </div>
                    </motion.div>

                    {/* MONSTER COMBAT CARD with HIT ANIMATION */}
                    <motion.div
                      animate={
                        isMonsterTakingDamage
                          ? {
                              x: [10, -10, 7, -7, 4, -4, 0],
                              y: [2, -2, 1, -1, 0],
                              filter: ['brightness(1)', 'brightness(1.5)', 'brightness(1)'],
                            }
                          : { x: 0, y: 0 }
                      }
                      transition={{ duration: 0.4, ease: 'easeInOut' }}
                      className={`relative bg-[#0D1117]/95 rounded-2xl p-4 border-2 transition-colors duration-200 overflow-hidden ${
                        isMonsterTakingDamage
                          ? 'border-amber-400 shadow-[0_0_30px_rgba(251,191,36,0.45)] bg-amber-950/25 ring-2 ring-amber-400/50'
                          : 'border-[#30363D] shadow-md'
                      }`}
                    >
                      {/* Floating Monster Damage Indicator */}
                      <AnimatePresence>
                        {isMonsterTakingDamage && monsterDamageTaken && (
                          <motion.div
                            initial={{ opacity: 0, y: 0, scale: 0.8 }}
                            animate={{ opacity: 1, y: -26, scale: monsterDamageTaken.isCrit ? 1.4 : 1.15 }}
                            exit={{ opacity: 0, y: -45, scale: 1 }}
                            transition={{ duration: 0.55 }}
                            className={`absolute top-2 right-3 z-20 px-2.5 py-1 text-white font-black text-xs sm:text-sm rounded-lg shadow-xl border flex items-center gap-1 ${
                              monsterDamageTaken.isCrit
                                ? 'bg-amber-600/90 border-amber-300 text-yellow-100 shadow-amber-500/50'
                                : 'bg-rose-600/90 border-rose-400'
                            }`}
                          >
                            <span>{monsterDamageTaken.isCrit ? '💥 CRIT! -' : '⚔️ -'}{monsterDamageTaken.amount} DMG</span>
                          </motion.div>
                        )}
                      </AnimatePresence>

                      <div className="flex items-center gap-3 mb-3">
                        <div className="w-12 h-12 rounded-xl bg-purple-900/30 border-2 border-purple-500/60 flex items-center justify-center text-purple-300 shadow flex-shrink-0">
                          <Skull className="w-6 h-6" />
                        </div>
                        <div className="min-w-0">
                          <span className="font-black text-white text-base block truncate">
                            {activeMonster ? activeMonster.name : 'Penjaga Ruang Bawah Tanah'}
                          </span>
                          <span className="text-xs text-purple-300 font-semibold">
                            {activeMonster ? 'Monster Elit Dungeon' : 'Menunggu Tantangan'}
                          </span>
                        </div>
                      </div>

                      {/* Monster HP bar */}
                      <div className="space-y-1">
                        <div className="flex justify-between text-[11px] font-bold">
                          <span className="text-slate-400">HP Monster</span>
                          <span className="text-purple-300">
                            {activeMonster ? `${activeMonster.currentHp} / ${activeMonster.maxHp}` : '--- / ---'}
                          </span>
                        </div>
                        <div className="h-2.5 bg-[#161B22] rounded-full overflow-hidden border border-[#30363D]">
                          <motion.div
                            className="h-full bg-gradient-to-r from-purple-600 to-pink-500 rounded-full"
                            animate={{
                              width: activeMonster
                                ? `${Math.max(0, Math.min(100, (activeMonster.currentHp / activeMonster.maxHp) * 100))}%`
                                : '100%',
                            }}
                            transition={{ duration: 0.3 }}
                          />
                        </div>
                      </div>

                      <div className="mt-3 pt-2.5 border-t border-[#30363D] flex items-center justify-between text-[11px] text-slate-400">
                        <span>Tipe: <strong className="text-white">Dungeon Boss</strong></span>
                        <span>Status: <strong className={isBattling ? 'text-amber-400 animate-pulse' : 'text-slate-400'}>
                          {isBattling ? 'Sedang Bertarung...' : 'Siaga'}
                        </strong></span>
                      </div>
                    </motion.div>
                  </div>
                </div>

                {/* Live Battle Log Box with Framer Motion slide & fade animations */}
                <div className="bg-[#161B22] border border-[#30363D] rounded-2xl p-5 min-h-[260px] shadow-lg">
                  <div className="flex items-center justify-between mb-4">
                    <div className="flex items-center gap-2">
                      <Swords className="w-4 h-4 text-[#FFB300]" />
                      <h4 className="text-xs font-black uppercase tracking-wider text-[#FFB300]">
                        LOG PERTEMPURAN REAL-TIME
                      </h4>
                    </div>
                    {battleLogs.length > 0 && (
                      <span className="text-[11px] text-slate-400 font-semibold bg-[#212836] px-2.5 py-0.5 rounded-full border border-[#30363D]">
                        {battleLogs.length} Aksi Terdaftar
                      </span>
                    )}
                  </div>

                  {battleLogs.length === 0 ? (
                    <div className="text-center py-12 text-slate-500 text-xs font-medium flex flex-col items-center justify-center gap-2">
                      <Swords className="w-8 h-8 text-slate-600 mb-1 animate-pulse" />
                      <span>Tekan tombol di atas untuk memulai simulasi pertempuran dungeon.</span>
                      <span className="text-[11px] text-slate-600">Animasi slide-in log dan efek getar damage akan aktif secara otomatis.</span>
                    </div>
                  ) : (
                    <div className="space-y-2.5 text-xs max-h-[380px] overflow-y-auto pr-1">
                      <AnimatePresence initial={false}>
                        {battleLogs.map((log) => (
                          <motion.div
                            key={log.id}
                            initial={{ opacity: 0, x: -24, y: 8, scale: 0.95 }}
                            animate={{ opacity: 1, x: 0, y: 0, scale: 1 }}
                            exit={{ opacity: 0, x: 20, scale: 0.9 }}
                            transition={{ type: 'spring', stiffness: 420, damping: 25, duration: 0.35 }}
                            className={`p-3 rounded-xl border font-medium flex items-center justify-between gap-3 shadow-md transition-all ${
                              log.isCrit
                                ? 'bg-rose-500/15 border-rose-500/50 text-rose-200 font-bold shadow-rose-950/40 ring-1 ring-rose-500/30'
                                : log.isDamageToHero
                                ? 'bg-red-950/30 border-red-500/40 text-red-200 font-semibold'
                                : log.isPlayer
                                ? 'bg-blue-950/30 border-blue-500/40 text-blue-200'
                                : log.text.includes('🏆')
                                ? 'bg-amber-500/15 border-amber-500/50 text-[#FFD54F] font-black'
                                : 'bg-[#212836] border-[#30363D] text-slate-300'
                            }`}
                          >
                            <div className="flex items-center gap-2.5 min-w-0">
                              <span className="text-sm flex-shrink-0">
                                {log.isCrit ? '💥' : log.isDamageToHero ? '🛡️' : log.isPlayer ? '⚔️' : log.text.includes('🏆') ? '🏆' : '⚠️'}
                              </span>
                              <span className="leading-relaxed">{log.text}</span>
                            </div>
                            <span className="text-[10px] text-slate-500 font-mono flex-shrink-0 whitespace-nowrap">
                              {log.time}
                            </span>
                          </motion.div>
                        ))}
                      </AnimatePresence>
                    </div>
                  )}
                </div>
              </div>
            )}

            {/* TAB 4: AKUN & PENGATURAN */}
            {activeTab === 'ACCOUNT' && (
              <div className="space-y-6">
                <div className="bg-[#161B22] border border-[#30363D] rounded-2xl p-5">
                  <h3 className="text-xs font-black uppercase tracking-wider text-[#FFB300] mb-3">
                    INFORMASI AKUN
                  </h3>
                  <div className="space-y-2 text-xs">
                    <div className="flex justify-between py-1.5 border-b border-[#30363D]">
                      <span className="text-slate-400">Username Pemain</span>
                      <span className="font-bold text-white">{currentUser?.username}</span>
                    </div>
                    <div className="flex justify-between py-1.5 border-b border-[#30363D]">
                      <span className="text-slate-400">Karakter Aktif</span>
                      <span className="font-bold text-[#FFD54F]">{activeCharacter.nickname} ({HERO_CLASSES[activeCharacter.heroClass].displayName})</span>
                    </div>
                    <div className="flex justify-between py-1.5">
                      <span className="text-slate-400">Total Karakter Dimiliki</span>
                      <span className="font-bold text-white">{characters.filter(c => c.userId === currentUser?.username).length} Karakter</span>
                    </div>
                  </div>
                </div>

                {/* Character Switcher */}
                <div className="bg-[#161B22] border border-[#30363D] rounded-2xl p-5">
                  <div className="flex items-center justify-between mb-4">
                    <h3 className="text-xs font-black uppercase tracking-wider text-[#FFB300]">
                      DAFTAR KARAKTER KAMU
                    </h3>
                    <button
                      type="button"
                      onClick={() => setScreen('CLASS_SELECT')}
                      className="px-3 py-1 bg-[#212836] hover:bg-[#2B3447] text-[#FFD54F] border border-[#FFB300]/30 rounded-lg text-xs font-bold flex items-center gap-1"
                    >
                      <Plus className="w-3.5 h-3.5" />
                      <span>Buat Karakter Baru</span>
                    </button>
                  </div>

                  <div className="space-y-2">
                    {characters.filter(c => c.userId === currentUser?.username).map(c => {
                      const isActive = c.id === activeCharacterId;
                      return (
                        <div
                          key={c.id}
                          onClick={() => {
                            setActiveCharacterId(c.id);
                            if (currentUser) {
                              setCurrentUser({ ...currentUser, activeCharacterId: c.id });
                            }
                            setToastMessage(`Beralih ke karakter ${c.nickname}!`);
                          }}
                          className={`p-3 rounded-xl border flex items-center justify-between cursor-pointer transition ${
                            isActive
                              ? 'bg-[#212836] border-[#FFB300]'
                              : 'bg-[#0D1117] border-[#30363D] hover:bg-[#161B22]'
                          }`}
                        >
                          <div className="flex items-center gap-3">
                            {renderClassIcon(c.heroClass, 'w-5 h-5')}
                            <div>
                              <div className="font-bold text-sm text-white">{c.nickname}</div>
                              <div className="text-[11px] text-slate-400">
                                {HERO_CLASSES[c.heroClass].displayName} • Lv.{c.level}
                              </div>
                            </div>
                          </div>
                          {isActive && (
                            <span className="px-2 py-0.5 rounded bg-[#FFB300] text-[#0D1117] font-black text-[10px]">
                              AKTIF
                            </span>
                          )}
                        </div>
                      );
                    })}
                  </div>
                </div>

                {/* Logout Button */}
                <button
                  type="button"
                  onClick={handleLogout}
                  className="w-full py-3 bg-[#212836] hover:bg-red-500/20 hover:border-red-500/50 text-slate-300 hover:text-red-400 border border-[#30363D] rounded-xl text-xs font-bold transition flex items-center justify-center gap-2"
                >
                  <LogOut className="w-4 h-4" />
                  <span>KELUAR DARI AKUN (LOGOUT)</span>
                </button>
              </div>
            )}
          </main>

          {/* ITEM INSPECTION MODAL */}
          {inspectedItem && (
            <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4">
              <div className="bg-[#161B22] border-2 border-[#FFB300]/80 rounded-3xl max-w-sm w-full p-6 shadow-2xl relative">
                <button
                  type="button"
                  onClick={() => setInspectedItem(null)}
                  className="absolute top-4 right-4 text-slate-400 hover:text-white"
                >
                  <X className="w-5 h-5" />
                </button>

                <div className="flex items-center gap-3 mb-4">
                  <div className="w-12 h-12 rounded-xl bg-[#0D1117] border border-[#30363D] flex items-center justify-center text-[#FFB300]">
                    <Sword className="w-6 h-6" />
                  </div>
                  <div>
                    <h3 className="font-bold text-base text-white">{inspectedItem.name}</h3>
                    <div className="flex items-center gap-2 mt-0.5">
                      <span className={`px-2 py-0.5 text-[9px] font-black rounded border ${getRarityBadgeColor(inspectedItem.rarity)}`}>
                        {inspectedItem.rarity}
                      </span>
                      <span className="text-xs text-slate-400">{inspectedItem.type}</span>
                    </div>
                  </div>
                </div>

                {/* Class requirement */}
                <div className="bg-[#212836] px-3 py-2 rounded-xl text-xs mb-4 flex justify-between">
                  <span className="text-slate-400">Syarat Kelas:</span>
                  <span className="font-bold text-[#FFD54F]">
                    {inspectedItem.requiredClass ? HERO_CLASSES[inspectedItem.requiredClass].displayName : 'Universal (Semua)'}
                  </span>
                </div>

                {/* Stats */}
                <div className="space-y-1.5 text-xs bg-[#0D1117] p-3 rounded-xl border border-[#30363D] mb-4">
                  {inspectedItem.atkBonus > 0 && (
                    <div className="flex justify-between text-amber-400 font-bold">
                      <span>Physical ATK:</span>
                      <span>+{inspectedItem.atkBonus}</span>
                    </div>
                  )}
                  {inspectedItem.matkBonus > 0 && (
                    <div className="flex justify-between text-purple-400 font-bold">
                      <span>Magic ATK:</span>
                      <span>+{inspectedItem.matkBonus}</span>
                    </div>
                  )}
                  {inspectedItem.defBonus > 0 && (
                    <div className="flex justify-between text-teal-400 font-bold">
                      <span>Defense:</span>
                      <span>+{inspectedItem.defBonus}</span>
                    </div>
                  )}
                  {inspectedItem.hpBonus > 0 && (
                    <div className="flex justify-between text-red-400 font-bold">
                      <span>HP:</span>
                      <span>+{inspectedItem.hpBonus}</span>
                    </div>
                  )}
                  {inspectedItem.mpBonus > 0 && (
                    <div className="flex justify-between text-blue-400 font-bold">
                      <span>MP:</span>
                      <span>+{inspectedItem.mpBonus}</span>
                    </div>
                  )}
                  {inspectedItem.critBonus > 0 && (
                    <div className="flex justify-between text-rose-400 font-bold">
                      <span>Crit Rate:</span>
                      <span>+{inspectedItem.critBonus}%</span>
                    </div>
                  )}
                  {inspectedItem.speedBonus > 0 && (
                    <div className="flex justify-between text-yellow-400 font-bold">
                      <span>Speed:</span>
                      <span>+{inspectedItem.speedBonus}</span>
                    </div>
                  )}
                </div>

                <p className="text-xs text-slate-400 mb-6 italic">
                  "{inspectedItem.description}"
                </p>

                {/* Modal Action Buttons */}
                <div className="grid grid-cols-2 gap-3">
                  {inspectedItem.isEquipped ? (
                    <button
                      type="button"
                      onClick={() => handleUnequipItem(inspectedItem)}
                      className="py-2.5 bg-[#212836] hover:bg-[#2B3447] text-white rounded-xl text-xs font-bold transition col-span-2"
                    >
                      Lepas Equipment
                    </button>
                  ) : (
                    <>
                      <button
                        type="button"
                        onClick={() => handleEquipItem(inspectedItem)}
                        className="py-2.5 bg-[#FFB300] hover:bg-[#FFA000] text-[#0D1117] font-black rounded-xl text-xs transition"
                      >
                        Pasang
                      </button>
                      <button
                        type="button"
                        onClick={() => handleSellItem(inspectedItem)}
                        className="py-2.5 bg-[#212836] hover:bg-amber-500/20 text-[#FFD54F] border border-[#FFB300]/40 rounded-xl text-xs font-bold transition"
                      >
                        Jual (Gold)
                      </button>
                    </>
                  )}
                </div>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
