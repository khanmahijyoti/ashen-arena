import json
from pathlib import Path

root = Path(__file__).parent / 'resources/assets/ashen_arena/models/item'
root.mkdir(parents=True, exist_ok=True)
elements = []

def box(start, end, material, uv=(4, 4, 12, 12)):
    elements.append({'from': start, 'to': end,
                     'faces': {face: {'uv': list(uv), 'texture': '#' + material}
                               for face in ('north', 'south', 'east', 'west', 'up', 'down')}})

box([7, -2, 7], [9, 6.7, 9], 'grip')
for y in (-1, 1, 3, 5):
    box([6.95, y, 6.95], [9.05, y + .65, 9.05], 'wrap')
box([6.8, -2.6, 6.8], [9.2, -1.7, 9.2], 'gold')
box([5.2, 6.6, 6.4], [10.8, 7.5, 9.6], 'gold')
box([6.2, 6.6, 5.7], [9.8, 7.5, 10.3], 'gold')
box([7.3, 7.5, 7.5], [8.9, 8.5, 8.5], 'gold')
box([7.5, 8.5, 7.65], [8.9, 22, 8.35], 'steel')
box([7.75, 22, 7.65], [9.15, 27, 8.35], 'steel')
box([8.05, 27, 7.7], [9.15, 29, 8.3], 'steel')
box([8.45, 29, 7.75], [9.15, 30.5, 8.25], 'steel')
box([8.8, 8.5, 7.6], [9.02, 22, 8.4], 'edge')
box([9.05, 22, 7.6], [9.27, 27, 8.4], 'edge')

model = {
    'parent': 'minecraft:block/block',
    'ambientocclusion': False,
    'gui_light': 'front',
    'textures': {'particle': 'minecraft:block/iron_block',
                 'steel': 'minecraft:block/iron_block',
                 'edge': 'minecraft:block/quartz_block_top',
                 'grip': 'minecraft:block/polished_blackstone',
                 'wrap': 'minecraft:block/red_nether_bricks',
                 'gold': 'minecraft:block/gold_block'},
    'elements': elements,
    'display': {
        'gui': {'rotation': [0, 0, -45], 'translation': [-2, -2, 0], 'scale': [.55, .55, .55]},
        'firstperson_righthand': {'rotation': [0, -90, 25], 'translation': [1.1, 1.4, .8], 'scale': [.72, .72, .72]},
        'firstperson_lefthand': {'rotation': [0, 90, -25], 'translation': [1.1, 1.4, .8], 'scale': [.72, .72, .72]},
        'thirdperson_righthand': {'rotation': [0, -90, 55], 'translation': [0, 4, .5], 'scale': [.78, .78, .78]},
        'thirdperson_lefthand': {'rotation': [0, 90, -55], 'translation': [0, 4, .5], 'scale': [.78, .78, .78]},
        'ground': {'translation': [0, 2, 0], 'scale': [.4, .4, .4]},
        'fixed': {'rotation': [0, 180, -45], 'translation': [-2, -2, 0], 'scale': [.55, .55, .55]}
    }
}
(root / 'ashen_katana.json').write_text(json.dumps(model, indent=2) + '\n', encoding='utf-8')
print('Wrote native Ashen Katana model')
