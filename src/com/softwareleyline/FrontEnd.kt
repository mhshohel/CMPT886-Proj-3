package com.softwareleyline

import com.google.gson.GsonBuilder
import org.apache.commons.cli.GnuParser
import org.apache.commons.cli.Options
import org.apache.commons.cli.Parser
import java.io.FileInputStream
import java.io.InputStreamReader

/**
 * Created by Geoff on 5/2/2016.
 */
fun main(args : Array<String>){

    val options = Options().apply {
        addOption("c", "className", true, "the (fully-qualified) name of the class to rewrite, eg 'com.softwareleyline.ExampleCode'")
        addOption("g", "methodGraph", true, "a path to the json-encoded method call graph to instrument, eg 'D:/Users/Code/heffe/resources/SampleGraph.json'")
        addOption("m", "targetMethod", true, "the name of a zero-arg method to call on <className> to test, eg 'runDag'")
    }

    var parser = GnuParser();

    val cmd = parser.parse(options, args)

    val targetClass = cmd.getOptionValue("className", "com.softwareleyline.ExampleCode")
    val graph = cmd.getOptionValue("methodGraph", "SampleGraph.json")
    val targetMethod = cmd.getOptionValue("targetMethod", "runDAG")

    println("heffe instrumenting $graph in $targetClass, running with $targetMethod...")

    val runner = FrontEnd(targetClass, graph, targetMethod);

    val result = runner.run()

    println("took path $result")
}

class FrontEnd(val targetClass : String, val serializedGraphPath : String, val targetMethod : String){

    private val gson = GsonBuilder().setPrettyPrinting().create();

    fun run() : Int{
        val driver = Assig3Driver();

        val resource = javaClass.classLoader.getResource(serializedGraphPath)

        val graphStream = if(resource != null){ resource.openStream(); }
                          else{ FileInputStream(serializedGraphPath) }

        val graphSurrogate = gson.fromJson(InputStreamReader(graphStream), GraphSurrogate::class.java)

        val graph = graphSurrogate.asGraph();

        val closure = {
            val clazz = Class.forName(targetClass);
            val method = clazz.getMethod(targetMethod);

            val instance = clazz.newInstance()

            method.invoke(instance)

            Unit;
        }

        val (result, it) = driver.determinePathFor(targetClass, graph, closure)

        return result;

    }
}