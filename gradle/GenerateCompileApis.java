import com.google.gson.*;
import org.objectweb.asm.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;

public final class GenerateCompileApis implements Opcodes {
    static String text(JsonObject o,String k){return o.has(k)&&!o.get(k).isJsonNull()?o.get(k).getAsString():null;}
    static int number(JsonObject o,String k){return o.get(k).getAsInt();}
    static JsonArray array(JsonObject o,String k){return o.has(k)?o.getAsJsonArray(k):new JsonArray();}
    static String[] strings(JsonObject o,String k){List<String> r=new ArrayList<>();for(JsonElement e:array(o,k))r.add(e.getAsString());return r.toArray(new String[0]);}
    static Object scalar(JsonObject o){String v=text(o,"value");return switch(text(o,"type")){
        case "Long"->Long.valueOf(v);case "Integer"->Integer.valueOf(v);case "Float"->Float.valueOf(v);case "Double"->Double.valueOf(v);case "Byte"->Byte.valueOf(v);case "Short"->Short.valueOf(v);case "Character"->v.charAt(0);default->throw new IllegalArgumentException(o.toString());};}
    static Object constant(JsonElement e){if(e==null||e.isJsonNull())return null;if(e.isJsonObject())return scalar(e.getAsJsonObject());return e.getAsString();}
    static void annotationValue(AnnotationVisitor visitor,String name,JsonElement value){
        if(value.isJsonArray()){AnnotationVisitor nested=visitor.visitArray(name);for(JsonElement e:value.getAsJsonArray())annotationValue(nested,null,e);nested.visitEnd();}
        else if(value.isJsonObject()){JsonObject o=value.getAsJsonObject();switch(text(o,"kind")){
            case "scalar"->visitor.visit(name,scalar(o));case "type"->visitor.visit(name,Type.getType(text(o,"descriptor")));
            case "enum"->visitor.visitEnum(name,text(o,"descriptor"),text(o,"name"));
            case "annotation"->{JsonObject a=o.getAsJsonObject("annotation");writeAnnotation(visitor.visitAnnotation(name,text(a,"descriptor")),a);}
            default->throw new IllegalArgumentException(o.toString());}}
        else if(value.getAsJsonPrimitive().isBoolean())visitor.visit(name,value.getAsBoolean());
        else visitor.visit(name,value.getAsString());
    }
    static void writeAnnotation(AnnotationVisitor visitor,JsonObject annotation){if(visitor==null)return;for(var e:annotation.getAsJsonObject("values").entrySet())annotationValue(visitor,e.getKey(),e.getValue());visitor.visitEnd();}
    interface Factory {AnnotationVisitor create(JsonObject annotation);}
    static void annotations(JsonObject o,String key,Factory factory){for(JsonElement a:array(o,key)){JsonObject obj=a.getAsJsonObject();writeAnnotation(factory.create(obj),obj);}}
    static TypePath path(JsonObject o){return TypePath.fromString(text(o,"typePath"));}
    static byte[] compile(JsonObject c){
        ClassWriter cw=new ClassWriter(0);cw.visit(number(c,"version"),number(c,"access"),text(c,"name"),text(c,"signature"),text(c,"superName"),strings(c,"interfaces"));
        if(c.has("outerClass"))cw.visitOuterClass(text(c,"outerClass"),text(c,"outerMethod"),text(c,"outerMethodDescriptor"));
        if(c.has("nestHostClass"))cw.visitNestHost(text(c,"nestHostClass"));
        for(String n:strings(c,"nestMembers"))cw.visitNestMember(n);for(String n:strings(c,"permittedSubclasses"))cw.visitPermittedSubclass(n);
        annotations(c,"annotations",a->cw.visitAnnotation(text(a,"descriptor"),a.get("visible").getAsBoolean()));
        annotations(c,"typeAnnotations",a->cw.visitTypeAnnotation(number(a,"typeRef"),path(a),text(a,"descriptor"),a.get("visible").getAsBoolean()));
        for(JsonElement el:array(c,"innerClasses")){var n=el.getAsJsonObject();cw.visitInnerClass(text(n,"name"),text(n,"outerName"),text(n,"innerName"),number(n,"access"));}
        for(JsonElement el:array(c,"recordComponents")){var r=el.getAsJsonObject();var rv=cw.visitRecordComponent(text(r,"name"),text(r,"descriptor"),text(r,"signature"));annotations(r,"annotations",a->rv.visitAnnotation(text(a,"descriptor"),a.get("visible").getAsBoolean()));annotations(r,"typeAnnotations",a->rv.visitTypeAnnotation(number(a,"typeRef"),path(a),text(a,"descriptor"),a.get("visible").getAsBoolean()));rv.visitEnd();}
        for(JsonElement el:array(c,"fields")){var f=el.getAsJsonObject();var fv=cw.visitField(number(f,"access"),text(f,"name"),text(f,"descriptor"),text(f,"signature"),constant(f.get("constant")));annotations(f,"annotations",a->fv.visitAnnotation(text(a,"descriptor"),a.get("visible").getAsBoolean()));annotations(f,"typeAnnotations",a->fv.visitTypeAnnotation(number(a,"typeRef"),path(a),text(a,"descriptor"),a.get("visible").getAsBoolean()));fv.visitEnd();}
        for(JsonElement el:array(c,"methods")){var m=el.getAsJsonObject();int access=number(m,"access");String descriptor=text(m,"descriptor");var mv=cw.visitMethod(access,text(m,"name"),descriptor,text(m,"signature"),strings(m,"exceptions"));
            for(JsonElement pe:array(m,"parameters")){var p=pe.getAsJsonObject();mv.visitParameter(text(p,"name"),number(p,"access"));}
            annotations(m,"annotations",a->mv.visitAnnotation(text(a,"descriptor"),a.get("visible").getAsBoolean()));annotations(m,"typeAnnotations",a->mv.visitTypeAnnotation(number(a,"typeRef"),path(a),text(a,"descriptor"),a.get("visible").getAsBoolean()));
            annotations(m,"parameterAnnotations",a->mv.visitParameterAnnotation(number(a,"parameter"),text(a,"descriptor"),a.get("visible").getAsBoolean()));
            if(m.has("annotationDefault")){var av=mv.visitAnnotationDefault();annotationValue(av,null,m.get("annotationDefault"));av.visitEnd();}
            if((access&(ACC_ABSTRACT|ACC_NATIVE))==0){mv.visitCode();mv.visitTypeInsn(NEW,"java/lang/UnsupportedOperationException");mv.visitInsn(DUP);mv.visitLdcInsn("Compile-only API header; use the real server or library at runtime");mv.visitMethodInsn(INVOKESPECIAL,"java/lang/UnsupportedOperationException","<init>","(Ljava/lang/String;)V",false);mv.visitInsn(ATHROW);int locals=(access&ACC_STATIC)==0?1:0;for(Type t:Type.getArgumentTypes(descriptor))locals+=t.getSize();mv.visitMaxs(3,locals);}
            mv.visitEnd();}
        cw.visitEnd();return cw.toByteArray();
    }
    static SortedMap<String,JsonObject> declarations(Path input,Path schema,Set<Path> active)throws Exception {
        if(!active.add(schema))throw new IllegalArgumentException("Cyclic API declaration inheritance: "+schema);
        JsonObject data;try(Reader r=Files.newBufferedReader(schema)){data=JsonParser.parseReader(r).getAsJsonObject();}
        SortedMap<String,JsonObject> classes=new TreeMap<>();
        if(data.has("base"))classes.putAll(declarations(input,input.resolve(text(data,"base")+".json"),active));
        for(JsonElement entry:array(data,"classes")){JsonObject c=entry.getAsJsonObject();classes.put(text(c,"name"),c);}
        active.remove(schema);return classes;
    }
    public static void main(String[] args)throws Exception{
        Path input=Path.of(args[0]),output=Path.of(args[1]);Files.createDirectories(output);
        for(Path schema:Files.list(input).filter(p->p.toString().endsWith(".json")).sorted().toList()){
            SortedMap<String,JsonObject> classes=declarations(input,schema,new HashSet<>());
            String name=schema.getFileName().toString().replace(".json",".jar");Path jar=output.resolve(name);
            try(var out=new JarOutputStream(Files.newOutputStream(jar))){for(JsonObject c:classes.values()){var entry=new JarEntry(text(c,"name")+".class");entry.setTime(0);out.putNextEntry(entry);out.write(compile(c));out.closeEntry();}}
            System.out.println("Generated compile API "+name+" ("+classes.size()+" declarations)");
        }
    }
}
