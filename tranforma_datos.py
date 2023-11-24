import json
import os


rutaCarpeta="prueba_ficheros"
json_file_path = 'jasonprueba.json'
rutaDocker=""

def vaciar_carpeta(ruta_carpeta):
    archivos = os.listdir(ruta_carpeta)
    for archivo in archivos:
        ruta_archivo = os.path.join(ruta_carpeta, archivo)
        try:
            if os.path.isfile(ruta_archivo):
                os.remove(ruta_archivo)
            elif os.path.isdir(ruta_archivo):
                pass
        except Exception as e:
            print(f"No se pudo eliminar {ruta_archivo}: {e}")





vaciar_carpeta(rutaCarpeta)


json_file_path = 'jasonprueba.json'


with open(json_file_path, 'r') as json_file:
    data = json.load(json_file)

def escribir_propiedad(archivo, propiedad, name):
    if propiedad == None:
        exit
    if type(propiedad)==list:
        Cadena=""
        for g in propiedad:
            Cadena=Cadena+g+","
        Cadena=Cadena[:-1]
        archivo.write(name+'='+Cadena+'\n')
    else:
        archivo.write(name+'='+str(propiedad)+'\n')


lista_mem=[]
for n in data:
    for t in n.get("bots"):
        with open(rutaCarpeta+ "/"+t.get("botId")+"properties", 'w') as archivo:
            #TODO refactorizar 
            escribir_propiedad(archivo, n.get("botType", None), "botType")
            escribir_propiedad(archivo, n.get("order", None), "order")
            escribir_propiedad(archivo, n.get("keyToPublish", None), "KeyToPublish")
            escribir_propiedad(archivo, n.get("orderToPublish", None), "orderToPublish")
            escribir_propiedad(archivo, n.get("rabbitOptions").get("mainQueue",None), "rabbitOptions.mainQueue")
            escribir_propiedad(archivo, n.get("rabbitOptions").get("bindings",None), "rabbitOptions.bindings")
            escribir_propiedad(archivo, n.get("rabbitOptions").get("queueByBot",None), "rabbitOptions.queueByBot")
            escribir_propiedad(archivo, t.get("botId", None), "bot.botId")
            lista_mem.append(t.get("botId"))
            escribir_propiedad(archivo, t.get("propertyFilePath", None), "bot.propertyFilePath")
            escribir_propiedad(archivo, t.get("isPersistent", None), "bot.isPersistent")

with open("prueba_ficheros/docker-compose.yml", 'w') as archivo:
    archivo.write("version: '1'"+'\n')
    archivo.write("services:"+'\n')
    for n in lista_mem:
        archivo.write("  bot_"+n+":"+'\n')
        archivo.write("    build:"+rutaDocker+'\n')
        archivo.write("    ports:"+'\n')
        archivo.write('      - "5672:5672"'+'\n')
        archivo.write("    environment:"+'\n')
        archivo.write('      - BOTICA_BOT_CONFIG_FILE:"/run/secrets/botica_config_'+n+'"'+'\n')
        archivo.write("    secrets:"+'\n')
        archivo.write("      - botica_config_"+n+""+'\n')

    archivo.write("secrets:"+'\n')
    for d in lista_mem:
        archivo.write("  botica_config_"+d+":"+'\n')
        archivo.write("    file: ./prueba_ficheros/"+d+"properties"+'\n')


        
