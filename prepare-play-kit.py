from pathlib import Path
from html import escape
from PIL import Image, ImageDraw, ImageFont
import shutil

root = Path(__file__).resolve().parent
out = root / 'output' / 'play-store'
out.mkdir(exist_ok=True, parents=True)
policy = (root / 'assets/privacy-policy.txt').read_text(encoding='utf-8')
(out / 'privacy-policy.html').write_text('<!doctype html><html lang="en"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Dynamic Alarm Privacy Policy</title><style>body{max-width:760px;margin:40px auto;padding:0 24px;font:18px/1.65 system-ui;color:#162d38;background:#f5f4ef}p{white-space:pre-wrap}h1{line-height:1.2}</style><h1>Dynamic Alarm Privacy Policy</h1>' + ''.join('<p>'+escape(p)+'</p>' for p in policy.split('\n\n')) + '</html>', encoding='utf-8')
shutil.copyfile(root/'PLAY-STORE-GUIDE.md', out/'START-HERE.md')

teal, ink, cream = '#087e8b', '#162d38', '#f5f4ef'
def clock(draw, x, y, size):
    def p(a,b): return (x+a*size/48, y+b*size/48)
    draw.ellipse([p(9,10),p(39,40)], outline='white', width=max(1,round(size/16)))
    draw.line([p(24,17),p(24,26),p(31,30)], fill='white', width=max(1,round(size/16)))
    for a,b in [((10,8),(5,14)),((38,8),(43,14)),((15,37),(12,42)),((33,37),(36,42))]:
        draw.line([p(*a),p(*b)],fill='white',width=max(1,round(size/16)))

icon = Image.new('RGB',(1024,1024),teal)
clock(ImageDraw.Draw(icon),0,0,1024)
icon.resize((512,512),Image.Resampling.LANCZOS).convert('RGBA').save(out/'icon-512.png')
canvas=Image.new('RGB',(2048,1000),cream)
d=ImageDraw.Draw(canvas)
d.rounded_rectangle((110,220,590,700),radius=100,fill=teal)
clock(d,134,244,432)
font= 'C:/Windows/Fonts/segoeui.ttf'
bold= 'C:/Windows/Fonts/segoeuib.ttf'
d.text((700,268),'Dynamic Alarm',font=ImageFont.truetype(bold,104),fill=ink)
d.text((708,414),'Your alarm, with a purpose',font=ImageFont.truetype(font,55),fill=teal)
d.text((710,512),'by Kibria',font=ImageFont.truetype(font,42),fill=ink)
canvas.resize((1024,500),Image.Resampling.LANCZOS).save(out/'feature-graphic-1024x500.png')
(out/'store-listing.txt').write_text('''APP NAME
Dynamic Alarm

SHORT DESCRIPTION
Set alarms with quotes, directions, email drafts and everyday actions.

FULL DESCRIPTION
Your alarm, with a purpose, by Kibria.

Plan a reminder and choose what comes next. Dynamic Alarm combines scheduled alarms with useful actions you can open when it is time.

• Set one-time or repeating alarms, choose a ringtone and adjust vibration.
• Add an uplifting quote, choose a morning or Fajr quote, or write your own.
• Search a destination on the map and open directions when your alarm rings.
• Prepare an email with selected attachments, then review and send it in your email app.
• Open a call, text draft, document, website or installed app.
• Request weather for a city when you open the weather action.
• Snooze for five minutes or dismiss from the ringing screen.
• Edit or delete saved alarms from the home screen.

No account and no ads. Alarm settings stay on your device. Optional map and weather features contact their service providers and require internet access. External actions need a compatible installed app; messages and emails are sent only when you confirm them there.

Allow notifications, alarms and full-screen alarm access in Setup for the intended ringing experience. Device battery settings can affect delivery. Set prayer alarm times yourself; the app does not calculate prayer times.

For Android 10 and above.
Suggestions or questions: asmksn@gmail.com

RELEASE NOTES — 1.3
Alarm deletion, searchable map destinations, selectable and custom quotes, and email attachments. Updated Android compatibility and an in-app privacy policy.
''',encoding='utf-8')
print('Publishing text, privacy page, icon and feature graphic created.')

