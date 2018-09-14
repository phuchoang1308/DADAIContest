del /s log.txt
echo Gamemode=NO_GUI>>config.txt
battle.exe Game.exe BotDemo.jar BotDemoB.exe 10 No_Log
REM battle.exe Game.exe BotDemo.exe BotDemoB.exe 100
REM battle.exe Game.exe BotDemo.exe BotDemoB.exe 100
del /s config.txt