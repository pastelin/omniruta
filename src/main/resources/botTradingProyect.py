import ccxt

exchange = ccxt.binance({
    'apiKey': 'TU_API_KEY',
    'secret': 'TU_SECRET',
    'options': {'defaultType': 'spot'},
    'enableRateLimit': True
})

try:
    # Obtener balance de USDT (modo prueba)
    balance = exchange.fetch_balance()
    usdt_balance = balance['USDT']['free']
    print(f"✅ Conexión exitosa! Balance: {usdt_balance} USDT")
    
    # Verificar permisos
    account_status = exchange.private_get_account()
    can_trade = account_status['canTrade']
    print(f"🔓 Permisos de trading: {'ACTIVADOS' if can_trade else 'BLOQUEADOS'}")
    
except Exception as e:
    print(f"❌ Error: {e}")