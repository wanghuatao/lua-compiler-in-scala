/*
    Copyright 2010 Timothy Morton
    
    This file is part of Slem.

    Slem is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Slem is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with Slem.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.slem

object IRTree {
    import org.kiama.attribution.Attributable
    import org.kiama.attribution.Attribution._
    import org.kiama.util.Messaging._
    
    ////////////BASICS////////////
    sealed abstract class L_Node extends Attributable
    
    abstract class L_BaseModule extends L_Node
    
    case class L_Module(globals : List[L_Global], metadata : List[L_BaseMetadata] = List()) extends L_BaseModule
    
    //Module level inline assembly
    case class L_AsmModule(asm : String) extends L_BaseModule
    
    case class L_Program(modules : List[L_BaseModule]) extends L_Node 
    abstract class L_Metadata extends L_Node with L_Value
    
    
    abstract class L_Instruction extends L_Node
    {
        var mappedMetadataIdn : L_Metadata = null
        var mappedMetadataVal : L_Metadata = null
        def mapMetadata(mdidn : L_Metadata, mdval : L_Metadata) = 
        {
            mappedMetadataIdn = mdidn
            mappedMetadataVal = mdval
        }
    }
    abstract class L_TerminatorInstruction extends L_Instruction
    abstract class L_Constant extends L_Node with L_Value
    
    ////////////GLOBALS////////////
    abstract class L_Global extends L_Node
    
    ////////////GLOBAL VARIABLES////////////
    case class L_GlobalVariable(
        value        : L_Constant,
        addressSpace : Int = 0,          //Optional
        isConstant   : Boolean = false,  //Optional
        section      : String = "",      //Optional
        alignment    : Int = 0,          //Optional
        name         : String = "",      //Optional
        linkage      : String = ""       //Optional
        ) extends L_Global with L_Value
    
    ////////////BLOCKS////////////
    case class L_Block(instructions : List[L_Instruction], terminator : L_TerminatorInstruction, label : L_Label = L_Label("")) extends L_Node
    {
    /*
        def addInstruction(newInstr : L_Instruction)
        {
            instructions = instructions ::: List(newInstr)
        }
        def terminate(termin : L_TerminatorInstruction)
        {
            terminator = termin
        }
    */
    }
    
    
    ////////////LABELS////////////
    case class L_Label(label : String) extends L_Node
    implicit def stringToLabel(s : String) : L_Label = L_Label(s)
    
    abstract class L_Function() extends L_Global with L_Value
    
    ////////////FUNCTION DECLARATIONS////////////
    case class L_FunctionDeclaration(
        returnType              : L_Type,
        funcName                : String = "",               //Optional
        arguments               : List[L_Argument] = List(), //Optional
        linkage                 : String = "",               //Optional
        visibilityStyle         : String = "",               //Optional
        callConvention          : String = "",               //Optional
        returnAttributes        : List[String] = List(),     //Optional
        alignment               : Int = 0,                   //Optional
        garbageCollector        : String = ""                //Optional
        ) extends L_Function
    
    case class L_FunctionReference(var funcPtr : L_Function = null) extends L_Function
    
    ////////////FUNCTION DEFINITIONS////////////
    case class L_FunctionDefinition(
        returnType       	    : L_Type,
        blocks                  : List[L_Block],
        funcName         	    : String = "",               //Optional
        arguments        	    : List[L_Argument] = List(), //Optional
        linkage          	    : String = "",			     //Optional
        visibilityStyle         : String = "",			     //Optional
        callConvention   	    : String = "",			     //Optional
        returnAttributes 	    : List[String] = List(),     //Optional
        funcAttributes   	    : List[String] = List(),     //Optional
        section                 : String = "",			     //Optional
        alignment               : Int = 0,		             //Optional
        garbageCollector        : String = ""   		     //Optional
        ) extends L_Function
    
    case class L_Argument(ty : L_Type, value : L_Value = null, attrs : List[String] = List(), argName : String = "") extends L_Node with L_Value
    implicit def valueToArgument(valuein : L_Value) : L_Argument = L_Argument(valuein->resultType, value = valuein)
    implicit def typeToArgument(typ : L_Type) : L_Argument = L_Argument(typ)
    
    /* Deprecated - unnessacary
    def L_NamedArgument(valuein : L_Value, namein : String) : L_Argument =
    {
        new L_Argument(valuein->resultType, value = valuein, argName = namein)
    }
    */
    
    ////////////TYPES////////////
    abstract class L_Type
    case class L_IntType(size : Int) extends L_Type
    case class L_FloatType() extends L_Type
    case class L_DoubleType() extends L_Type
    case class L_FP128Type() extends L_Type
    case class L_X86FP80Type() extends L_Type
    case class L_PPCFP128Type() extends L_Type
    case class L_VoidType() extends L_Type
    case class L_LabelType() extends L_Type
    case class L_MetadataType() extends L_Type
    case class L_VarArgsType() extends L_Type

    
    ////////////DERIVED TYPES////////////
    case class L_ArrayType(numElements : Long, elementType : L_Type) extends L_Type
    case class L_FunctionType(returnType : L_Type, parameterList : List[L_Type]) extends L_Type
    case class L_StructureType(fields : List[L_Type]) extends L_Type
    case class L_PackedStructureType(fields : List[L_Type]) extends L_Type
    case class L_PointerType(pointer : L_Type) extends L_Type
    case class L_VectorType(numElements : Long, elementType : L_Type) extends L_Type
    case class L_OpaqueType() extends L_Type
    case class L_UpReferenceType(levels : Int) extends L_Type
    //TODO: Complete Type Up-references
    
    ////////////METADATA////////////
    case class L_MetadataString(str : String) extends L_Metadata
    abstract class L_BaseMetadata extends L_Metadata
    case class L_MetadataNode(fields : List[L_Value]) extends L_BaseMetadata
    case class L_NamedMetadata(name : String, fields : List[L_MetadataNode]) extends L_BaseMetadata
    
    ////////////VALUES////////////
    trait L_Value extends L_Node
    
    ////////////SIMPLE CONSTANT VALUES////////////
    case class L_Boolean(value : Boolean) extends L_Constant

    
    case class L_Int(size : Int, value : Long) extends L_Constant
    implicit def boolToConst(b : Boolean) : L_Int =
    {
        if(b)
        {
            L_Int(1, 1)
        }
        else
        {
            L_Int(1, 0)
        }
    }
    implicit def intToConst(i : Int) : L_Int = L_Int(32, i)
    implicit def longToConst(l : Long) : L_Int = L_Int(64, l)

    case class L_Float(value : String) extends L_Constant
    implicit def floatToConst(f : Float) : L_Float = L_Float("" + f)

    case class L_Double(value : String) extends L_Constant
    implicit def doubltToConst(d : Double) : L_Double = L_Double("" + d)
    
    case class L_FP128(value : String) extends L_Constant
    case class L_X86FP80(value : String) extends L_Constant
    case class L_PPCFP128(value : String) extends L_Constant
    
    
    case class L_NullPointer(pty : L_Type) extends L_Constant
    
    case class L_Void() extends L_Constant
    
    ////////////COMPLEX CONSTANT VALUES////////////
    case class L_PackedStructure(elements : List[L_Value]) extends L_Constant
    case class L_Structure(elements : List[L_Value]) extends L_Constant
    case class L_Array(elements : List[L_Value]) extends L_Constant
    case class L_String(s : String) extends L_Constant
    
    
    case class L_Vector(elements : List[L_Value]) extends L_Constant
    case class L_ZeroInitialiser(typ : L_Type) extends L_Constant
    
    //Block addresses constant for indirect branches
    case class L_BlockAddress(functionTarget : L_Value, blockTarget : L_Label) extends L_Constant
    
    //TODO - implement metadata nodes
    //case class L_MetadataNode() extends L_Constant
    
    
    ////////////BINARY OPERATOR INSTRUCTIONS////////////
    abstract class L_BinOpInstruction(LHSin : L_Value, RHSin : L_Value) extends L_Instruction with L_Value
    {
        val LHS = LHSin;
        val RHS = RHSin;
        val instructionString = "Unknown Binary Instruction"
    }
    
    case class L_Add(LHSin : L_Value, RHSin : L_Value) extends L_BinOpInstruction(LHSin, RHSin)
    {
        override val instructionString = "add"
    }
    
    case class L_NSWAdd(LHSin : L_Value, RHSin : L_Value) extends L_BinOpInstruction(LHSin, RHSin)
    {
        override val instructionString = "add nsw"
    }
    
    case class L_NUWAdd(LHSin : L_Value, RHSin : L_Value) extends L_BinOpInstruction(LHSin, RHSin)
    {
        override val instructionString = "add nuw"
    }
    
    case class L_NUWNSWAdd(LHSin : L_Value, RHSin : L_Value) extends L_BinOpInstruction(LHSin, RHSin)
    {
        override val instructionString = "add nuw nsw"
    }
    
    case class L_FAdd(LHSin : L_Value, RHSin : L_Value) extends L_BinOpInstruction(LHSin, RHSin) 
    {
        override val instructionString = "fadd"
    }
    
    case class L_Sub(LHSin : L_Value, RHSin : L_Value) extends L_BinOpInstruction(LHSin, RHSin) 
    {
        override val instructionString = "sub"
    }
    
    case class L_NSWSub(LHSin : L_Value, RHSin : L_Value) extends L_BinOpInstruction(LHSin, RHSin) 
    {
        override val instructionString = "sub nsw"
    }
    
    case class L_NUWSub(LHSin : L_Value, RHSin : L_Value) extends L_BinOpInstruction(LHSin, RHSin) 
    {
        override val instructionString = "sub nuw"
    }
    
    case class L_NUWNSWSub(LHSin : L_Value, RHSin : L_Value) extends L_BinOpInstruction(LHSin, RHSin) 
    {
        override val instructionString = "sub nuw nsw"
    }
    
    case class L_FSub(LHSin : L_Value, RHSin : L_Value) extends L_BinOpInstruction(LHSin, RHSin) 
    {
        override val instructionString = "fsub"
    }
        
    case class L_Mul(LHSin : L_Value, RHSin : L_Value) extends L_BinOpInstruction(LHSin, RHSin) 
    {
        override val instructionString = "mul"
    }
            
    case class L_NSWMul(LHSin : L_Value, RHSin : L_Value) extends L_BinOpInstruction(LHSin, RHSin) 
    {
        override val instructionString = "mul nsw"
    }
            
    case class L_NUWMul(LHSin : L_Value, RHSin : L_Value) extends L_BinOpInstruction(LHSin, RHSin) 
    {
        override val instructionString = "mul nuw"
    }
    
    case class L_NUWNSWMul(LHSin : L_Value, RHSin : L_Value) extends L_BinOpInstruction(LHSin, RHSin) 
    {
        override val instructionString = "mul nuw nsw"
    }
                
    case class L_FMul(LHSin : L_Value, RHSin : L_Value) extends L_BinOpInstruction(LHSin, RHSin) 
    {
        override val instructionString = "fmul"
    }
            
    case class L_UDiv(LHSin : L_Value, RHSin : L_Value) extends L_BinOpInstruction(LHSin, RHSin) 
    {
        override val instructionString = "udiv"
    }
    
    case class L_ExactUDiv(LHSin : L_Value, RHSin : L_Value) extends L_BinOpInstruction(LHSin, RHSin)
    {
        override val instructionString = "udiv exact"
    }
            
    case class L_SDiv(LHSin : L_Value, RHSin : L_Value) extends L_BinOpInstruction(LHSin, RHSin) 
    {
        override val instructionString = "sdiv"
    }
            
    case class L_ExactSDiv(LHSin : L_Value, RHSin : L_Value) extends L_BinOpInstruction(LHSin, RHSin) 
    {
        override val instructionString = "sdiv exact"
    }
        
    case class L_FDiv(LHSin : L_Value, RHSin : L_Value) extends L_BinOpInstruction(LHSin, RHSin) 
    {
        override val instructionString = "fdiv"
    }
            
    case class L_URem(LHSin : L_Value, RHSin : L_Value) extends L_BinOpInstruction(LHSin, RHSin) 
    {
        override val instructionString = "urem"
    }
            
    case class L_SRem(LHSin : L_Value, RHSin : L_Value) extends L_BinOpInstruction(LHSin, RHSin) 
    {
        override val instructionString = "srem"
    }
                
    case class L_FRem(LHSin : L_Value, RHSin : L_Value) extends L_BinOpInstruction(LHSin, RHSin) 
    {
        override val instructionString = "frem"
    }
        
    case class L_Shl(LHSin : L_Value, RHSin : L_Value) extends L_BinOpInstruction(LHSin, RHSin) 
    {
        override val instructionString = "shl"
    }
    
    case class L_NUWShl(LHSin : L_Value, RHSin : L_Value) extends L_BinOpInstruction(LHSin, RHSin) 
    {
        override val instructionString = "shl nuw"
    }
    
    case class L_NSWShl(LHSin : L_Value, RHSin : L_Value) extends L_BinOpInstruction(LHSin, RHSin) 
    {
        override val instructionString = "shl nsw"
    }
    
    case class L_NUWNSWShl(LHSin : L_Value, RHSin : L_Value) extends L_BinOpInstruction(LHSin, RHSin) 
    {
        override val instructionString = "shl nuw nsw"
    }
    
    case class L_LShr(LHSin : L_Value, RHSin : L_Value) extends L_BinOpInstruction(LHSin, RHSin) 
    {
        override val instructionString = "lshr"
    }

    case class L_ExactLShr(LHSin : L_Value, RHSin : L_Value) extends L_BinOpInstruction(LHSin, RHSin) 
    {
        override val instructionString = "lshr exact"
    }
    
    case class L_AShr(LHSin : L_Value, RHSin : L_Value) extends L_BinOpInstruction(LHSin, RHSin) 
    {
        override val instructionString = "ashr"
    }

    case class L_ExactAShr(LHSin : L_Value, RHSin : L_Value) extends L_BinOpInstruction(LHSin, RHSin) 
    {
        override val instructionString = "ashr exact"
    }    
    
    case class L_And(LHSin : L_Value, RHSin : L_Value) extends L_BinOpInstruction(LHSin, RHSin) 
    {
        override val instructionString = "and"
    }
    
    case class L_Or(LHSin : L_Value, RHSin : L_Value) extends L_BinOpInstruction(LHSin, RHSin) 
    {
        override val instructionString = "or"
    }
    
    case class L_Xor(LHSin : L_Value, RHSin : L_Value) extends L_BinOpInstruction(LHSin, RHSin) 
    {
        override val instructionString = "xor"
    }
    
    
    ////////////MEMORY INSTRUCTIONS////////////
    case class L_Alloca(typ : L_Type, numElements : L_Value = null, alignment : Long = 0) extends L_Instruction with L_Value

    //DONE : need to implement metadata in order to make this fully functional. , nonTemporal : Boolean = false, nonTempIndex : Long = 0
    case class L_Load(typ : L_Type, pointer : L_Value, isVolatile : Boolean = false, alignment : Long = 0) extends L_Instruction with L_Value

    //DONE : need to implement metadata in order to make this fully functional., nonTemporal : Boolean = false, nonTempIndex : Long = 0
    case class L_Store(value : L_Value, pointer : L_Value, isVolatile : Boolean = false, alignment : Long = 0) extends L_Instruction
    
    /* Deprecated - we can simply infer the type from the values
    case class L_TypeIndex(ty : L_Type, idx : L_Value) extends L_Node
    implicit def longToTypeIndex(l : Long) : L_TypeIndex = L_TypeIndex(L_IntType(64), l)
    implicit def intToTypeIndex(i : Int) : L_TypeIndex = L_TypeIndex(L_IntType(32), i)
    */
    
    case class L_GetElementPtr(pty : L_Type, pval : L_Value, typeIndexes : List[L_Value], inBounds : Boolean = false) extends L_Instruction with L_Value
    {
        def getResultType() : L_Type =
        {
            getResultType(pty, typeIndexes, List())
        }
        
        def dereferenceUpRef(upref : L_UpReferenceType, prevPtrTypeList : List[L_Type]) : L_Type =
        {
            if(upref.levels == 1)
            {
                return L_PointerType(upref);
            }
            else if(upref.levels > 1)
            {
                if(prevPtrTypeList.length - upref.levels >= 0)
                {
                    return prevPtrTypeList(prevPtrTypeList.length - upref.levels)
                }
                else
                {
                    return L_OpaqueType() //Type error has occured
                }                            
            }
            else
            {
                L_OpaqueType() //Type error has occured
            }
        }
        
        def getResultType(ptypein : L_Type, indexes : List[L_Value], prevPtrTypeListin : List[L_Type]) : L_Type =
        {
            var ptype = ptypein
            var prevPtrTypeList = prevPtrTypeListin
            //Dereference any type up-references:
            ptype match
            {
                case t : L_UpReferenceType =>
                {
                    if(t.levels == 1)
                    {
                        return L_PointerType(t);
                    }
                    else if(t.levels > 1)
                    {

                        if(prevPtrTypeList.length - t.levels >= 0)
                        {
                            ptype = prevPtrTypeList(prevPtrTypeList.length - t.levels)
                            prevPtrTypeList = List()
                        }
                        else
                        {
                            return L_OpaqueType() //Type error has occured
                        }                            
                        
                    }
                    else
                    {
                        return L_OpaqueType() //Type error has occured
                    }
                } 
                case _ => {}
            }
            
            
            if(indexes.size <= 0)
            {
                L_OpaqueType() //Type Error Has Occured
            }
            else
            {
                ptype match
                {
                    case t : L_ArrayType =>
                    {
                        val nextType = t.elementType
                        val idxTail = indexes.tail
                        if(idxTail.size > 0)
                        {
                            getResultType(nextType, idxTail, prevPtrTypeList ::: List(t))
                        }
                        else
                        {
                            nextType match
                            {
                                case t : L_UpReferenceType => dereferenceUpRef(t, prevPtrTypeList)
                                case _ => L_PointerType(nextType)
                            }
                        }
                    }
                    case t : L_VectorType =>
                    {
                        val nextType = t.elementType
                        val idxTail = indexes.tail
                        if(idxTail.size > 0)
                        {
                            getResultType(nextType, idxTail, prevPtrTypeList ::: List(t))
                        }
                        else
                        {
                            nextType match
                            {
                                case t : L_UpReferenceType => dereferenceUpRef(t, prevPtrTypeList)
                                case _ => L_PointerType(nextType)
                            }
                        }
                    }
                    case t : L_PointerType =>
                    {
                        val nextType = t.pointer
                        val idxTail = indexes.tail
                        if(idxTail.size > 0)
                        {
                            getResultType(nextType, idxTail, prevPtrTypeList ::: List(t))
                        }
                        else
                        {
                            nextType match
                            {
                                case t : L_UpReferenceType => dereferenceUpRef(t, prevPtrTypeList)
                                case _ => L_PointerType(nextType)
                            }
                        }
                    }
                    case t : L_StructureType =>
                    {
                        indexes.head match
                        {
                            case c : L_Int =>
                            {
                                val nextType = t.fields(c.value.toInt) 			// We can convert this to an int because
                                                                                // a structure with more than max_int fields
                                                                                // is ludicrously large  - and useless - the 
                                                                                // source code would have to define over max_int 
                                                                                // fields one by one!
                                val idxTail = indexes.tail
                                if(idxTail.size > 0)
                                {
                                    getResultType(nextType, idxTail, prevPtrTypeList ::: List(t))
                                }
                                else
                                {
                                    nextType match
                                    {
                                        case t : L_UpReferenceType => dereferenceUpRef(t, prevPtrTypeList)
                                        case _ => L_PointerType(nextType)
                                    }
                                }
                            }
                            case _ =>
                            {
                                L_OpaqueType() //Type error has occured
                            }
                        }					
                    }
                    case t : L_PackedStructureType =>
                    {
                        indexes.head match
                        {
                            case c : L_Int =>
                            {
                                val nextType = t.fields(c.value.toInt) 			// We can convert this to an int because
                                                                                // a structure with more than max_int fields
                                                                                // is ludicrously large  - and useless - the 
                                                                                // source code would have to define over max_int 
                                                                                // fields one by one!
                                val idxTail = indexes.tail
                                if(idxTail.size > 0)
                                {
                                    getResultType(nextType, idxTail, prevPtrTypeList ::: List(t))
                                }
                                else
                                {
                                    nextType match
                                    {
                                        case t : L_UpReferenceType => dereferenceUpRef(t, prevPtrTypeList)
                                        case _ => L_PointerType(nextType)
                                    }
                                }
                            }
                            case _ =>
                            {
                                L_OpaqueType() //Type error has occured
                            }
                        }					
                    }
                    case _ => 
                    {
                        L_OpaqueType() //Type error has occured
                    }
                }
            }
        }
    }
    
    
    ////////////CONVERSION OPERATIONS////////////
    
    abstract class L_ConversionOperation(valuein : L_Value, targetTypein : L_Type) extends L_Instruction with L_Value
    {
        val value = valuein
        val targetType = targetTypein
        val instructionString = "Unknown Conversion Operation"
    }
    
    case class L_Trunc(valuein : L_Value, targetTypein : L_Type) extends L_ConversionOperation(valuein, targetTypein)
    {
        override val instructionString = "trunc"
    }
    
    case class L_ZExt(valuein : L_Value, targetTypein : L_Type) extends L_ConversionOperation(valuein, targetTypein)
    {
        override val instructionString = "zext"
    }	
    
    case class L_SExt(valuein : L_Value, targetTypein : L_Type) extends L_ConversionOperation(valuein, targetTypein)
    {
        override val instructionString = "sext"
    }
    
    case class L_FPTrunc(valuein : L_Value, targetTypein : L_Type) extends L_ConversionOperation(valuein, targetTypein)
    {
        override val instructionString = "fptrunc"
    }
    
    case class L_FPExt(valuein : L_Value, targetTypein : L_Type) extends L_ConversionOperation(valuein, targetTypein)
    {
        override val instructionString = "fpext"
    }
    
    case class L_FPToUI(valuein : L_Value, targetTypein : L_Type) extends L_ConversionOperation(valuein, targetTypein)
    {
        override val instructionString = "fptoui"
    }
    
    case class L_FPToSI(valuein : L_Value, targetTypein : L_Type) extends L_ConversionOperation(valuein, targetTypein)
    {
        override val instructionString = "fptosi"
    }
    
    case class L_UIToFP(valuein : L_Value, targetTypein : L_Type) extends L_ConversionOperation(valuein, targetTypein)
    {
        override val instructionString = "uitofp"
    }
    
    case class L_SIToFP(valuein : L_Value, targetTypein : L_Type) extends L_ConversionOperation(valuein, targetTypein)
    {
        override val instructionString = "sitofp"
    }
    
    case class L_PtrToInt(valuein : L_Value, targetTypein : L_Type) extends L_ConversionOperation(valuein, targetTypein)
    {
        override val instructionString = "ptrtoint"
    }
    
    case class L_IntToPtr(valuein : L_Value, targetTypein : L_Type) extends L_ConversionOperation(valuein, targetTypein)
    {
        override val instructionString = "inttoptr"
    }
    
    case class L_Bitcast(valuein : L_Value, targetTypein : L_Type) extends L_ConversionOperation(valuein, targetTypein)
    {
        override val instructionString = "bitcast"
    }

    ////////////OTHER OPERATIONS////////////
    abstract class L_ICMP(LHSin : L_Value, RHSin : L_Value, compCodein : String) extends L_Instruction with L_Value
    {
        val LHS = LHSin
        val RHS = RHSin
        val compCode = compCodein
    }
    
    case class L_ICmpEQ(LHSin : L_Value, RHSin : L_Value) extends L_ICMP(LHSin, RHSin, "eq")
    
    case class L_ICmpNEQ(LHSin : L_Value, RHSin : L_Value) extends L_ICMP(LHSin, RHSin, "ne")
    case class L_ICmpNE(LHSin : L_Value, RHSin : L_Value) extends L_ICMP(LHSin, RHSin, "ne")
    
    case class L_ICmpUGT(LHSin : L_Value, RHSin : L_Value) extends L_ICMP(LHSin, RHSin, "ugt")
    
    case class L_ICmpUGE(LHSin : L_Value, RHSin : L_Value) extends L_ICMP(LHSin, RHSin, "uge")
    
    case class L_ICmpULT(LHSin : L_Value, RHSin : L_Value) extends L_ICMP(LHSin, RHSin, "ult")
    
    case class L_ICmpULE(LHSin : L_Value, RHSin : L_Value) extends L_ICMP(LHSin, RHSin, "ule")
    
    case class L_ICmpSGT(LHSin : L_Value, RHSin : L_Value) extends L_ICMP(LHSin, RHSin, "sgt")
    
    case class L_ICmpSGE(LHSin : L_Value, RHSin : L_Value) extends L_ICMP(LHSin, RHSin, "sge")
    
    case class L_ICmpSLT(LHSin : L_Value, RHSin : L_Value) extends L_ICMP(LHSin, RHSin, "slt")
    
    case class L_ICmpSLE(LHSin : L_Value, RHSin : L_Value) extends L_ICMP(LHSin, RHSin, "sle")
    
    
    abstract class L_FCMP(LHSin : L_Value, RHSin : L_Value, compCodein : String) extends L_Instruction with L_Value
    {
        val LHS = LHSin
        val RHS = RHSin
        val compCode = compCodein
    }	
    case class L_FCmpFalse(LHSin : L_Value, RHSin : L_Value) extends L_FCMP(LHSin, RHSin, "false")
        
    case class L_FCmpOEQ(LHSin : L_Value, RHSin : L_Value) extends L_FCMP(LHSin, RHSin, "oeq")
        
    case class L_FCmpOGT(LHSin : L_Value, RHSin : L_Value) extends L_FCMP(LHSin, RHSin, "ogt")
        
    case class L_FCmpOGE(LHSin : L_Value, RHSin : L_Value) extends L_FCMP(LHSin, RHSin, "oge")
        
    case class L_FCmpOLT(LHSin : L_Value, RHSin : L_Value) extends L_FCMP(LHSin, RHSin, "olt")
        
    case class L_FCmpONE(LHSin : L_Value, RHSin : L_Value) extends L_FCMP(LHSin, RHSin, "one")
        
    case class L_FCmpORD(LHSin : L_Value, RHSin : L_Value) extends L_FCMP(LHSin, RHSin, "ord")
        
    case class L_FCmpUEQ(LHSin : L_Value, RHSin : L_Value) extends L_FCMP(LHSin, RHSin, "ueq")
        
    case class L_FCmpUGT(LHSin : L_Value, RHSin : L_Value) extends L_FCMP(LHSin, RHSin, "ugt")
        
    case class L_FCmpUGE(LHSin : L_Value, RHSin : L_Value) extends L_FCMP(LHSin, RHSin, "uge")
        
    case class L_FCmpULT(LHSin : L_Value, RHSin : L_Value) extends L_FCMP(LHSin, RHSin, "ult")
        
    case class L_FCmpULE(LHSin : L_Value, RHSin : L_Value) extends L_FCMP(LHSin, RHSin, "ule")
        
    case class L_FCmpUNE(LHSin : L_Value, RHSin : L_Value) extends L_FCMP(LHSin, RHSin, "une")
        
    case class L_FCmpUNO(LHSin : L_Value, RHSin : L_Value) extends L_FCMP(LHSin, RHSin, "uno")
        
    case class L_FCmpTrue(LHSin : L_Value, RHSin : L_Value) extends L_FCMP(LHSin, RHSin, "true")
        

    case class L_ValueLabel(value : L_Value, label : L_Label) extends L_Node
    
    case class L_Phi(valueLabels : List[L_ValueLabel]) extends L_Instruction with L_Value
    
    case class L_Select(cond : L_Value, val1 : L_Value, val2 : L_Value) extends L_Instruction with L_Value
    
    case class L_Call(
        typ : L_Type, 
        fnptrval : L_Value, 
        fnargs : List[L_Argument], 
        tail : Boolean = false,                         //Optional
        callConvention : String = "",                   //Optional
        returnAttributes : List[String] = List(),       //Optional
        fnty : L_Type = null,                           //Optional - TODO : implement function type pointers
        fnattrs : List[String] = List()                 //Optional
        ) extends L_Instruction with L_Value
    
    case class L_Va_Arg(argList : L_Value, argType : L_Type) extends L_Instruction with L_Value
    
    ////////////TERMINATOR INSTRUCTIONS////////////
    case class L_Ret(rvalue : L_Value) extends L_TerminatorInstruction
    
    case class L_Br(dest : L_Label) extends L_TerminatorInstruction
    
    //Conditional Branch
    case class L_BrCond(cond : L_Value, ifTrue : L_Label, ifFalse : L_Label) extends L_TerminatorInstruction
    
    case class L_Switch(value : L_Value, default : L_Label, cases : List[L_ValueLabel]) extends L_TerminatorInstruction
    
    case class L_IndirectBr(address : L_Value, possibleDestinations : List[L_Label]) extends L_TerminatorInstruction
    
    case class L_Invoke(
        funcPtrVal : L_Value,      
        args : List[L_Argument],
        normal : L_Label,
        unwind : L_Label,
        funcTypePtr : L_Type = null,      //This is a function pointer type where there is ambiguity - such as varargs fns.
        callConv : String = "",               //Optional
        retAttrs : List[String] = List(),     //Optional
        attrs : List[String] = List()         //Optional
        ) extends L_TerminatorInstruction with L_Value
    
    case class L_Unwind() extends L_TerminatorInstruction
    
    case class L_Unreachable() extends L_TerminatorInstruction
    
    
    ////////////VECTOR OPERATIONS////////////
    case class L_ExtractElement(vec : L_Value, idx : L_Value) extends L_Instruction with L_Value
    
    case class L_InsertElement(vec : L_Value, elt : L_Value, idx : L_Value) extends L_Instruction with L_Value
    
    case class L_ShuffleVector(v1 : L_Value, v2 : L_Value, mask : L_Value) extends L_Instruction with L_Value
    
    ////////////AGGREGATE OPERATIONS////////////
    case class L_ExtractValue(value : L_Value, indexes : List[L_Int]) extends L_Instruction with L_Value
    
    case class L_InsertValue(value : L_Value, elt : L_Value, idx : L_Value) extends L_Instruction with L_Value
    
    val resultType : L_Node ==> L_Type = 
    {
        attr {
            //FUNCTION DECLARATIONS
            case n : L_FunctionDeclaration => //n.returnType //TODO: support varags signature types
            {
                var argTypes : List[L_Type] = List()
                for(a <- n.arguments)
                {
                    argTypes = argTypes ::: List(a.ty) 
                }
                L_PointerType(L_FunctionType(n.returnType, argTypes))
            }
            case n : L_FunctionDefinition => //n.returnType
            {
                var argTypes : List[L_Type] = List()
                for(a <- n.arguments)
                {
                    argTypes = argTypes ::: List(a.ty) 
                }
                L_PointerType(L_FunctionType(n.returnType, argTypes))            
            }
            
            //METADATA
            case n : L_Metadata       => L_MetadataType()
        
            //SIMPLE CONSTANTS
            case n : L_Boolean        => L_IntType(1)
            case n : L_Int            => L_IntType(n.size)
            case n : L_Float          => L_FloatType()
            case n : L_Double         => L_DoubleType()
            case n : L_FP128          => L_FP128Type()
            case n : L_X86FP80        => L_X86FP80Type()
            case n : L_PPCFP128       => L_PPCFP128Type()
    
            case n : L_NullPointer    => L_PointerType(n.pty)
            case n : L_Void           => L_VoidType()
            
            //COMPLEX CONSTANTS
            case n : L_Structure       => L_StructureType(n.elements.map(e => e->resultType))
            case n : L_PackedStructure => L_PackedStructureType(n.elements.map(e => e->resultType))
            case n : L_Array           => L_ArrayType(n.elements.size, n.elements.head->resultType)
            case n : L_String          => L_ArrayType(n.s.size - (n.s.filter(c => (c == '\\')).size * 2), L_IntType(8))
            case n : L_Vector          => L_VectorType(n.elements.size, n.elements.head->resultType)
            case n : L_ZeroInitialiser => n.typ
            
            //BINARY OPERATOR INSTRUCTIONS
            /* Deprecated code - simplified 
            case n : L_Add            => (n.LHS)->resultType
            case n : L_NSWAdd         => (n.LHS)->resultType
            case n : L_NUWAdd         => (n.LHS)->resultType
            case n : L_FAdd           => (n.LHS)->resultType
            case n : L_Sub            => (n.LHS)->resultType
            case n : L_NSWSub         => (n.LHS)->resultType
            case n : L_NUWSub         => (n.LHS)->resultType
            case n : L_FSub           => (n.LHS)->resultType
            case n : L_Mul            => (n.LHS)->resultType
            case n : L_NSWMul         => (n.LHS)->resultType
            case n : L_NUWMul         => (n.LHS)->resultType
            case n : L_FMul           => (n.LHS)->resultType
            case n : L_UDiv           => (n.LHS)->resultType
            case n : L_ExactUDiv      => (n.LHS)->resultType
            case n : L_SDiv           => (n.LHS)->resultType
            case n : L_ExactSDiv      => (n.LHS)->resultType
            case n : L_FDiv           => (n.LHS)->resultType
            case n : L_URem           => (n.LHS)->resultType
            case n : L_SRem           => (n.LHS)->resultType
            case n : L_FRem           => (n.LHS)->resultType
            case n : L_Shl            => (n.LHS)->resultType
            case n : L_LShr           => (n.LHS)->resultType
            case n : L_AShr           => (n.LHS)->resultType
            case n : L_And            => (n.LHS)->resultType
            case n : L_Or             => (n.LHS)->resultType
            case n : L_Xor            => (n.LHS)->resultType
            */
            case n : L_BinOpInstruction => (n.LHS)->resultType
            
            //MEMORY INSTRUCTIONS
            case n : L_Alloca         => L_PointerType(n.typ) //TODO: FIX THIS!!! Assumes that all stack memory locations are in 32 bits
            case n : L_Load           => 
            {
                (n.typ) match
                {
                    case p : L_PointerType => p.pointer
                    case _ => 
                    {
                        L_OpaqueType() //Type Error
                    }
                }
            }
            case n : L_GetElementPtr  => //TODO : Fix
            {
                /*
                n.pty match
                {
                    case t : L_ArrayType =>
                    {
                        L_PointerType(t.elementType)
                    }
                    case t : L_VectorType =>
                    {
                        L_PointerType(t.elementType)
                    }
                    case t : L_StructureType =>
                    {
                        L_VoidType() 
                    }
                    case _ =>
                    {
                        L_VoidType()
                    }
                }
                */
                n.getResultType()
            }
            
            //CONVERSION OPERATION INSTRUCTIONS
            case n : L_Trunc          => n.targetType
            case n : L_ZExt           => n.targetType
            case n : L_SExt           => n.targetType
            case n : L_FPTrunc        => n.targetType
            case n : L_FPExt          => n.targetType
            case n : L_FPToUI         => n.targetType
            case n : L_FPToSI         => n.targetType
            case n : L_UIToFP         => n.targetType
            case n : L_SIToFP         => n.targetType
            case n : L_PtrToInt       => n.targetType
            case n : L_IntToPtr       => n.targetType
            case n : L_Bitcast        => n.targetType
            
            //OTHER OPERATIONS
            case n : L_ICMP           => L_IntType(1)
            /* Removed and simplified
            case n : L_ICmpEQ         => L_IntType(1)
            case n : L_ICmpNE         => L_IntType(1)
            case n : L_ICmpNEQ        => L_IntType(1)
            case n : L_ICmpUGT        => L_IntType(1)
            case n : L_ICmpUGE        => L_IntType(1)
            case n : L_ICmpULT        => L_IntType(1)
            case n : L_ICmpULE        => L_IntType(1)
            case n : L_ICmpSGT        => L_IntType(1)
            case n : L_ICmpSGE        => L_IntType(1)
            case n : L_ICmpSLT        => L_IntType(1)
            case n : L_ICmpSLE        => L_IntType(1)
            */
            case n : L_FCMP           => L_IntType(1)
            /* Removed and simplified
            case n : L_FCmpFalse      => L_IntType(1)
            case n : L_FCmpOEQ        => L_IntType(1)
            case n : L_FCmpOGT        => L_IntType(1)
            case n : L_FCmpOGE        => L_IntType(1)
            case n : L_FCmpOLT        => L_IntType(1)
            case n : L_FCmpONE        => L_IntType(1)
            case n : L_FCmpORD        => L_IntType(1)
            case n : L_FCmpUEQ        => L_IntType(1)
            case n : L_FCmpUGT        => L_IntType(1)
            case n : L_FCmpUGE        => L_IntType(1)
            case n : L_FCmpULT        => L_IntType(1)
            case n : L_FCmpULE        => L_IntType(1)
            case n : L_FCmpUNE        => L_IntType(1)
            case n : L_FCmpUNO        => L_IntType(1)
            case n : L_FCmpTrue       => L_IntType(1)
            */
            case n : L_Phi            =>
            {
                if(n.valueLabels.size > 0)
                {
                    val firstElement = n.valueLabels.head
                    (firstElement.value)->resultType
                }
                else
                {
                    L_OpaqueType() //Type Error
                }
            }
            case n : L_Select         => (n.val1)->resultType
            case n : L_Call           => n.typ //TODO : Check this.

            case n : L_Va_Arg         => n.argType
            case n : L_Invoke         => n.funcTypePtr
            
            //VECTOR OPERATIONS
            case n : L_ExtractElement => 
            {
                val vecType = ((n.vec)->resultType)
                vecType match
                {
                    case v : L_VectorType => v.elementType
                    case _ => L_VoidType() //Type error
                }
            }
            case n : L_InsertElement  => (n.vec)->resultType
            case n : L_ShuffleVector  => 
            {
                val vecType   = (n.v1)->resultType
                vecType match
                {
                    case v : L_VectorType =>
                    {
                        val vecLength = (n.mask)->resultType
                        vecLength match
                        {
                            case v2 : L_VectorType =>
                            {
                                L_VectorType(v2.numElements, v.elementType)
                            }
                            case _ => L_OpaqueType() //Type Error
                        }
                    }
                    case _ => L_OpaqueType() //Type Error
                }
            }
            
            //AGGREGATE OPERATIONS - TODO : will require testing
            case n : L_ExtractValue   => //Match the type extracted from the aggregate structure
            {
                if(n.indexes.size == 0)
                {
                    L_OpaqueType() //Type error
                }
                else
                {
                    var out = (n.value)->resultType
                    var prevTypes : List[L_Type] = List()
                    for(idx <- n.indexes)
                    {
                        out match
                        {
                            case n2 : L_UpReferenceType =>
                            {
                                if(n2.levels == 1)
                                {
                                    //Do nothing - self referential up-reference
                                }
                                if(n2.levels > 1)
                                {
                                    if(prevTypes.length - n2.levels >= 0)
                                    {
                                        out = prevTypes(prevTypes.length - n2.levels)
                                        prevTypes = List()
                                    }
                                    else
                                    {
                                        out = L_OpaqueType() // Type Error
                                    }
                                }
                                else
                                {
                                    out = L_OpaqueType() // Type Error
                                }
                            }
                            case _ =>
                        }
                        prevTypes = prevTypes ::: List(out)
                        out match
                        {
                            case n2 : L_ArrayType      => out = n2.elementType
                            case n2 : L_StructureType  => 
                            {
                                if(n2.fields.size > idx.value.toInt)
                                {
                                    out = n2.fields.apply(idx.value.toInt)
                                }
                                else
                                {
                                    out = L_OpaqueType() //Type Error
                                }
                            }
                            case n2 : L_PackedStructureType => 
                            {
                                if(n2.fields.size > idx.value.toInt)
                                {
                                    out = n2.fields.apply(idx.value.toInt)
                                }
                                else
                                {
                                    out = L_OpaqueType() //Type Error
                                } 
                            }
                            case _  => out = L_OpaqueType() //Type error
                        }
                    }
                    out
                }
            }
            case n : L_InsertValue    => (n.value)->resultType
            
            
            case n : L_Argument => n.ty
            
            case n : L_GlobalVariable => L_PointerType((n.value)->resultType)
            case _ => L_OpaqueType() //Type error
        }
    }	
}
