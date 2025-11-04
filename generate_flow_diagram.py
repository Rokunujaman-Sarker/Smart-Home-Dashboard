from PIL import Image, ImageDraw, ImageFont
import os

# Comprehensive flow diagram for Smart Home Dashboard
WIDTH, HEIGHT = 1800, 1400
bg = (255, 255, 255)

# Color scheme
color_android = (76, 175, 80)  # Green
color_firebase = (255, 152, 0)  # Orange
color_esp32 = (33, 150, 243)  # Blue
color_hardware = (156, 39, 176)  # Purple
color_box_fill = (240, 248, 255)
color_box_outline = (30, 30, 30)
color_arrow = (60, 60, 60)
color_data_arrow = (0, 150, 136)  # Teal

img = Image.new('RGB', (WIDTH, HEIGHT), color=bg)
d = ImageDraw.Draw(img)

# Load fonts
try:
    font_small = ImageFont.truetype('arial.ttf', 14)
    font_normal = ImageFont.truetype('arial.ttf', 16)
    font_bold = ImageFont.truetype('arialbd.ttf', 18)
    font_title = ImageFont.truetype('arialbd.ttf', 32)
    font_section = ImageFont.truetype('arialbd.ttf', 22)
except Exception:
    font_small = ImageFont.load_default()
    font_normal = font_small
    font_bold = font_small
    font_title = font_small
    font_section = font_small

def draw_box(box, text, fill_color, outline_color=color_box_outline, text_color=(0,0,0)):
    """Draw a rounded rectangle box with centered text"""
    x1, y1, x2, y2 = box
    d.rectangle(box, fill=fill_color, outline=outline_color, width=3)

    # Center text
    lines = text.split('\n')
    total_height = len(lines) * 20
    y_offset = y1 + (y2 - y1 - total_height) // 2

    for line in lines:
        bbox = d.textbbox((0, 0), line, font=font_bold)
        text_width = bbox[2] - bbox[0]
        x_offset = x1 + (x2 - x1 - text_width) // 2
        d.text((x_offset, y_offset), line, fill=text_color, font=font_bold)
        y_offset += 22

def draw_small_box(box, text, fill_color, outline_color=color_box_outline):
    """Draw a small box with text"""
    x1, y1, x2, y2 = box
    d.rectangle(box, fill=fill_color, outline=outline_color, width=2)

    # Center text
    bbox = d.textbbox((0, 0), text, font=font_normal)
    text_width = bbox[2] - bbox[0]
    text_height = bbox[3] - bbox[1]
    x_offset = x1 + (x2 - x1 - text_width) // 2
    y_offset = y1 + (y2 - y1 - text_height) // 2
    d.text((x_offset, y_offset), text, fill=(0,0,0), font=font_normal)

def draw_arrow(x1, y1, x2, y2, color=color_arrow, width=3, label=""):
    """Draw an arrow from point 1 to point 2"""
    d.line([(x1, y1), (x2, y2)], fill=color, width=width)

    # Arrowhead
    import math
    angle = math.atan2(y2 - y1, x2 - x1)
    arrow_size = 12

    # Calculate arrowhead points
    point1_x = x2 - arrow_size * math.cos(angle - math.pi / 6)
    point1_y = y2 - arrow_size * math.sin(angle - math.pi / 6)
    point2_x = x2 - arrow_size * math.cos(angle + math.pi / 6)
    point2_y = y2 - arrow_size * math.sin(angle + math.pi / 6)

    d.polygon([(x2, y2), (point1_x, point1_y), (point2_x, point2_y)], fill=color)

    # Label
    if label:
        mid_x = (x1 + x2) // 2
        mid_y = (y1 + y2) // 2 - 10
        bbox = d.textbbox((0, 0), label, font=font_small)
        text_width = bbox[2] - bbox[0]
        d.rectangle([mid_x - text_width//2 - 3, mid_y - 2, mid_x + text_width//2 + 3, mid_y + 16], fill=bg)
        d.text((mid_x - text_width//2, mid_y), label, fill=color, font=font_small)

def draw_bidirectional_arrow(x1, y1, x2, y2, color=color_arrow, label=""):
    """Draw a bidirectional arrow"""
    draw_arrow(x1, y1, x2, y2, color, 3)
    # Draw reverse arrowhead
    import math
    angle = math.atan2(y1 - y2, x1 - x2)
    arrow_size = 12
    point1_x = x1 - arrow_size * math.cos(angle - math.pi / 6)
    point1_y = y1 - arrow_size * math.sin(angle - math.pi / 6)
    point2_x = x1 - arrow_size * math.cos(angle + math.pi / 6)
    point2_y = y1 - arrow_size * math.sin(angle + math.pi / 6)
    d.polygon([(x1, y1), (point1_x, point1_y), (point2_x, point2_y)], fill=color)

    if label:
        mid_x = (x1 + x2) // 2
        mid_y = (y1 + y2) // 2 - 10
        bbox = d.textbbox((0, 0), label, font=font_small)
        text_width = bbox[2] - bbox[0]
        d.rectangle([mid_x - text_width//2 - 3, mid_y - 2, mid_x + text_width//2 + 3, mid_y + 16], fill=bg)
        d.text((mid_x - text_width//2, mid_y), label, fill=color, font=font_small)

# Title
d.text((50, 20), 'Smart Home Dashboard - Complete System Architecture', fill=(0,0,0), font=font_title)
d.text((50, 60), 'Android App + Firebase + ESP32 IoT Control System', fill=(100,100,100), font=font_bold)

# ========== LAYER 1: USER & AUTHENTICATION =========
d.text((50, 110), '1. USER AUTHENTICATION LAYER', fill=color_android, font=font_section)

# User box
user_box = (80, 150, 280, 240)
draw_box(user_box, 'USER\n(Mobile Device)', (230, 255, 230), color_android, (0,100,0))

# LoginActivity
login_box = (350, 140, 550, 250)
draw_small_box(login_box, 'LoginActivity', (200, 255, 200), color_android)
d.text((360, 160), '• Email/Password', fill=(0,0,0), font=font_small)
d.text((360, 180), '• Google Sign-In', fill=(0,0,0), font=font_small)
d.text((360, 200), '• OAuth2', fill=(0,0,0), font=font_small)
d.text((360, 220), '• User Session', fill=(0,0,0), font=font_small)

# RegistrationActivity
register_box = (600, 140, 800, 250)
draw_small_box(register_box, 'RegistrationActivity', (200, 255, 200), color_android)
d.text((610, 160), '• New Account', fill=(0,0,0), font=font_small)
d.text((610, 180), '• Email/Password', fill=(0,0,0), font=font_small)
d.text((610, 200), '• Google OAuth', fill=(0,0,0), font=font_small)
d.text((610, 220), '• User Profile Setup', fill=(0,0,0), font=font_small)

# Firebase Auth
firebase_auth_box = (900, 150, 1100, 240)
draw_box(firebase_auth_box, 'Firebase\nAuthentication', (255, 230, 200), color_firebase, (200,100,0))

# Arrows
draw_arrow(280, 195, 350, 195, color_android, 3, "Login/Register")
draw_arrow(550, 180, 600, 180, color_android, 3)
draw_bidirectional_arrow(800, 195, 900, 195, color_firebase, "Auth API")

# ========== LAYER 2: MAIN DASHBOARD =========
d.text((50, 290), '2. DASHBOARD & ROOM MANAGEMENT', fill=color_android, font=font_section)

# DashboardActivity
dashboard_box = (80, 330, 380, 480)
draw_box(dashboard_box, 'DashboardActivity\n(Main Hub)', (200, 255, 200), color_android)
d.text((95, 380), '• Room List Display', fill=(0,0,0), font=font_small)
d.text((95, 400), '• Add/Edit/Delete Rooms', fill=(0,0,0), font=font_small)
d.text((95, 420), '• Master Switch Control', fill=(0,0,0), font=font_small)
d.text((95, 440), '• User Profile Display', fill=(0,0,0), font=font_small)
d.text((95, 460), '• ESP32 Settings Access', fill=(0,0,0), font=font_small)

# RoomActivity
room_box = (450, 330, 750, 480)
draw_box(room_box, 'RoomActivity\n(Device Control)', (200, 255, 200), color_android)
d.text((465, 380), '• Device List per Room', fill=(0,0,0), font=font_small)
d.text((465, 400), '• Add/Edit/Delete Devices', fill=(0,0,0), font=font_small)
d.text((465, 420), '• Toggle Device ON/OFF', fill=(0,0,0), font=font_small)
d.text((465, 440), '• Real-time State Sync', fill=(0,0,0), font=font_small)
d.text((465, 460), '• Device Type Selection', fill=(0,0,0), font=font_small)

# ESP32SettingsActivity
esp32_settings_box = (820, 330, 1050, 430)
draw_small_box(esp32_settings_box, 'ESP32SettingsActivity', (200, 255, 200), color_android)
d.text((835, 360), '• Firebase Test', fill=(0,0,0), font=font_small)
d.text((835, 380), '• Connection Check', fill=(0,0,0), font=font_small)
d.text((835, 400), '• Device Status', fill=(0,0,0), font=font_small)

# Arrows
draw_arrow(230, 240, 230, 330, color_android, 3, "After Auth")
draw_arrow(380, 405, 450, 405, color_android, 3, "Select Room")
draw_arrow(280, 460, 820, 360, color_android, 2, "Settings")

# ========== LAYER 3: FIREBASE REALTIME DATABASE (Cloud) =========
d.text((50, 530), '3. FIREBASE REALTIME DATABASE (Cloud)', fill=color_firebase, font=font_section)

firebase_db_box = (80, 570, 750, 780)
draw_box(firebase_db_box, 'Firebase Realtime Database', (255, 240, 220), color_firebase)

# Database structure
d.text((100, 610), 'Database Structure:', fill=(0,0,0), font=font_bold)
d.text((110, 640), '/users/{uid}/', fill=(150, 75, 0), font=font_normal)
d.text((130, 665), '├─ rooms/', fill=(0,0,0), font=font_small)
d.text((150, 685), '│   ├─ {roomName}/', fill=(0,0,0), font=font_small)
d.text((170, 705), '│   │   ├─ displayName: "Living Room"', fill=(0,0,0), font=font_small)
d.text((170, 725), '│   │   └─ devices/', fill=(0,0,0), font=font_small)
d.text((130, 745), '└─ globalDevices/', fill=(0,0,0), font=font_small)
d.text((150, 765), '     └─ {deviceId}: {state, type, room}', fill=(0,0,0), font=font_small)

# Connection info
d.text((450, 640), 'Real-time Listeners:', fill=(0,0,0), font=font_bold)
d.text((460, 665), '• Device state changes', fill=(0,0,0), font=font_small)
d.text((460, 685), '• Room updates', fill=(0,0,0), font=font_small)
d.text((460, 705), '• Master switch state', fill=(0,0,0), font=font_small)
d.text((460, 725), '• Bi-directional sync', fill=(0,0,0), font=font_small)
d.text((460, 745), '• Multi-device support', fill=(0,0,0), font=font_small)

# Arrows to Firebase
draw_bidirectional_arrow(230, 480, 230, 570, color_data_arrow, "Read/Write")
draw_bidirectional_arrow(600, 480, 600, 570, color_data_arrow, "Real-time Sync")

# ========== LAYER 4: ESP32 CONTROLLER =========
d.text((50, 830), '4. ESP32 IoT CONTROLLER', fill=color_esp32, font=font_section)

# ESP32 main box
esp32_box = (80, 870, 450, 1100)
draw_box(esp32_box, 'ESP32 Microcontroller', (220, 240, 255), color_esp32)

d.text((95, 910), 'Firmware Components:', fill=(0,0,100), font=font_bold)
d.text((105, 940), '• WiFi Connection ("Bundia vaja")', fill=(0,0,0), font=font_small)
d.text((105, 960), '• Firebase Client Library', fill=(0,0,0), font=font_small)
d.text((105, 980), '• Firebase Stream Listener', fill=(0,0,0), font=font_small)
d.text((105, 1000), '• User Authentication', fill=(0,0,0), font=font_small)
d.text((105, 1020), '• Device State Management', fill=(0,0,0), font=font_small)
d.text((105, 1040), '• 6x Relay Control (GPIO)', fill=(0,0,0), font=font_small)
d.text((105, 1060), '• 6x Physical Switch Input', fill=(0,0,0), font=font_small)
d.text((105, 1080), '• Real-time State Sync', fill=(0,0,0), font=font_small)

# Device mapping
device_map_box = (480, 870, 750, 1100)
draw_box(device_map_box, 'Device Mapping', (220, 240, 255), color_esp32)
d.text((495, 910), 'Relay Pins:', fill=(0,0,100), font=font_bold)
d.text((505, 935), 'RELAY1 (GPIO 4)  → light1', fill=(0,0,0), font=font_small)
d.text((505, 955), 'RELAY2 (GPIO 5)  → light2', fill=(0,0,0), font=font_small)
d.text((505, 975), 'RELAY3 (GPIO 18) → light3', fill=(0,0,0), font=font_small)
d.text((505, 995), 'RELAY4 (GPIO 19) → fan1', fill=(0,0,0), font=font_small)
d.text((505, 1015), 'RELAY5 (GPIO 21) → fan2', fill=(0,0,0), font=font_small)
d.text((505, 1035), 'RELAY6 (GPIO 22) → custom', fill=(0,0,0), font=font_small)

d.text((495, 1060), 'Switch Inputs:', fill=(0,0,100), font=font_bold)
d.text((505, 1080), 'GPIO 12-15, 25-26 (6 switches)', fill=(0,0,0), font=font_small)

# Firebase connection from ESP32
draw_bidirectional_arrow(265, 870, 415, 780, color_data_arrow, "Firebase Stream")

# ========== LAYER 5: HARDWARE LAYER =========
d.text((50, 1150), '5. PHYSICAL HARDWARE LAYER', fill=color_hardware, font=font_section)

# Relay Module
relay_module_box = (80, 1190, 350, 1320)
draw_box(relay_module_box, '6-Channel\nRelay Module', (240, 220, 255), color_hardware)
d.text((95, 1245), 'Relay Outputs:', fill=(100,0,100), font=font_bold)
d.text((105, 1270), '• CH1-CH6 (High/Low trigger)', fill=(0,0,0), font=font_small)
d.text((105, 1290), '• 220V AC switching capability', fill=(0,0,0), font=font_small)

# Physical switches
switches_box = (380, 1190, 650, 1320)
draw_box(switches_box, 'Physical Switches', (240, 220, 255), color_hardware)
d.text((395, 1245), 'Wall Switches:', fill=(100,0,100), font=font_bold)
d.text((405, 1270), '• 6x Toggle/Push switches', fill=(0,0,0), font=font_small)
d.text((405, 1290), '• Connected to ESP32 GPIO', fill=(0,0,0), font=font_small)

# Appliances
appliances_box = (680, 1190, 950, 1320)
draw_box(appliances_box, 'Home Appliances', (240, 220, 255), color_hardware)
d.text((695, 1245), 'Connected Devices:', fill=(100,0,100), font=font_bold)
d.text((705, 1270), '• Lights (3x)', fill=(0,0,0), font=font_small)
d.text((705, 1290), '• Fans (2x)', fill=(0,0,0), font=font_small)
d.text((705, 1300), '• Other appliances', fill=(0,0,0), font=font_small)

# Hardware connections
draw_arrow(265, 1100, 215, 1190, color_hardware, 3, "Control Signal")
draw_arrow(265, 1100, 515, 1190, color_hardware, 3, "Input Read")
draw_arrow(350, 1255, 680, 1255, color_hardware, 3, "AC Power")

# ========== DATA FLOW ANNOTATIONS =========
# Add legend
legend_box = (1100, 570, 1750, 780)
draw_box(legend_box, 'Data Flow Summary', (245, 245, 245), (100,100,100))

d.text((1120, 610), 'Control Flow:', fill=(0,0,0), font=font_bold)
d.text((1130, 635), '1. User opens app → Login/Register', fill=(0,0,0), font=font_small)
d.text((1130, 655), '2. Firebase authenticates user', fill=(0,0,0), font=font_small)
d.text((1130, 675), '3. Dashboard loads rooms from Firebase', fill=(0,0,0), font=font_small)
d.text((1130, 695), '4. User selects room → RoomActivity', fill=(0,0,0), font=font_small)
d.text((1130, 715), '5. User toggles device → Firebase update', fill=(0,0,0), font=font_small)
d.text((1130, 735), '6. ESP32 stream receives change', fill=(0,0,0), font=font_small)
d.text((1130, 755), '7. ESP32 triggers relay → Appliance ON/OFF', fill=(0,0,0), font=font_small)

d.text((1420, 610), 'Reverse Flow:', fill=(0,0,0), font=font_bold)
d.text((1430, 635), '1. Physical switch pressed', fill=(0,0,0), font=font_small)
d.text((1430, 655), '2. ESP32 reads GPIO input', fill=(0,0,0), font=font_small)
d.text((1430, 675), '3. ESP32 writes to Firebase', fill=(0,0,0), font=font_small)
d.text((1430, 695), '4. App receives real-time update', fill=(0,0,0), font=font_small)
d.text((1430, 715), '5. UI reflects new state', fill=(0,0,0), font=font_small)

# Network info box
network_box = (1100, 830, 1750, 1100)
draw_box(network_box, 'Network Architecture', (245, 245, 245), (100,100,100))

d.text((1120, 870), 'Key Features:', fill=(0,0,0), font=font_bold)
d.text((1130, 895), '✓ Works from anywhere (WiFi/Mobile data)', fill=(0,100,0), font=font_small)
d.text((1130, 915), '✓ Real-time bidirectional sync', fill=(0,100,0), font=font_small)
d.text((1130, 935), '✓ Multiple users can control simultaneously', fill=(0,100,0), font=font_small)
d.text((1130, 955), '✓ Physical switches work offline (local relay)', fill=(0,100,0), font=font_small)
d.text((1130, 975), '✓ App requires internet for Firebase', fill=(0,100,0), font=font_small)
d.text((1130, 995), '✓ ESP32 requires WiFi for Firebase connection', fill=(0,100,0), font=font_small)

d.text((1120, 1025), 'Security:', fill=(0,0,0), font=font_bold)
d.text((1130, 1050), '• Firebase Authentication (OAuth2)', fill=(0,0,0), font=font_small)
d.text((1130, 1070), '• User-specific database rules', fill=(0,0,0), font=font_small)

# Technologies box
tech_box = (1100, 1150, 1750, 1320)
draw_box(tech_box, 'Technology Stack', (245, 245, 245), (100,100,100))

d.text((1120, 1190), 'Android App:', fill=(0,0,0), font=font_bold)
d.text((1130, 1215), '• Java, Android SDK 34', fill=(0,0,0), font=font_small)
d.text((1130, 1235), '• Firebase SDK (Auth + Database)', fill=(0,0,0), font=font_small)
d.text((1130, 1255), '• Material Design Components', fill=(0,0,0), font=font_small)

d.text((1420, 1190), 'ESP32 Firmware:', fill=(0,0,0), font=font_bold)
d.text((1430, 1215), '• Arduino Framework', fill=(0,0,0), font=font_small)
d.text((1430, 1235), '• Firebase_ESP_Client library', fill=(0,0,0), font=font_small)
d.text((1430, 1255), '• WiFi.h for connectivity', fill=(0,0,0), font=font_small)

d.text((1120, 1280), 'Backend: Firebase Realtime Database (Google Cloud)', fill=(0,0,0), font=font_small)

# Save the image
output_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'flow_diagram.jpg')
img.save(output_path, 'JPEG', quality=95)
print(f'✓ Complete flow diagram saved: {output_path}')
print(f'  Size: {WIDTH}x{HEIGHT} pixels')
print(f'  Format: JPEG')
print(f'  Components: Authentication, Dashboard, Room Control, Firebase, ESP32, Hardware')
