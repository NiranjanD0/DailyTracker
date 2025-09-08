# DailyTracker

**Note:** This plugin is fully LLM-made.

DailyTracker is a Minecraft Paper plugin (1.21.8) that tracks **player uptime** and **server downtime**, excluding AFK time if enabled.

---

## Player Uptime

- Tracks **daily, weekly, and monthly uptime** per player  
- Excludes AFK time if `track-afk` is enabled  
- Saves stats per day, week, month for history commands  
- Resets:
  - Daily → server midnight  
  - Weekly → Sunday midnight  
  - Monthly → 1st of the month (configurable)

---

## Server Downtime

- Tracks total downtime  
- Monthly cycle start configurable (`monthly-downtime-start-date`)  
- Admin commands to view and reset downtime  
- Manual reset requires confirmation (`/dat downtime reset` → `/dat downtime reset confirm`)

---

## PlaceholderAPI Support

- `%dat_uptime_day%` / `%dat_uptime_day_raw%` → daily uptime  
- `%dat_uptime_week%` / `%dat_uptime_week_raw%` → weekly uptime  
- `%dat_uptime_month%` / `%dat_uptime_month_raw%` → monthly uptime  
- `%dat_downtime_month%` / `%dat_downtime_month_raw%` → monthly downtime  

---

## Commands

- `/dat stats` → open own GUI  
- `/dat stats <player>` → admin-only, view other players  
- `/dat top <daily|weekly|monthly>` → leaderboard  
- `/dat history <day|week|month> <date>` → own history  
- `/dat history <day|week|month> <date> <player>` → admin-only  
- `/dat downtime` → admin-only  
- `/dat downtime reset` → confirmation required  
- `/dat downtime reset confirm` → confirm reset  
- `/dat reload` → admin-only, reload config and GUI  

---

## Permissions

- `dailytracker.use` → view own stats  
- `dailytracker.admin` → view others’ stats, reload plugin  
- `dailytracker.admin.downtime` → manage server downtime  

> Permissions override OP status; OP is not required.

---

## Configuration (`config.yml`)

- `track-afk` → exclude AFK time from uptime  
- `monthly-player-reset-day` → player monthly stats reset day  
- `monthly-downtime-start-date` → starting date for server downtime  
- `auto-save-interval` → seconds between auto-saves  
- `time-format` → customize time display (e.g., "xh ym zs")  

---

## GUI (`gui.yml`)

- **Slot 46** → Combined uptime: Daily | Weekly | Monthly  
- **Slot 50** → Refresh button  
- **Slot 54** → Close button  
- Fully configurable: material, display name, lore, placeholders  

---

## Issues

Report bugs or feature requests on [GitHub Issues](https://github.com/niranjand0/DailyTracker/issues). Include: Minecraft version, plugin version, and any console errors.

---

## License

MIT License. Free to use, modify, and distribute. Attribution appreciated to [Niranjand0](https://github.com/niranjand0). No warranty provided.  
[Full License Text](https://opensource.org/licenses/MIT)
